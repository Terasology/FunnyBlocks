/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.portalblocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.notifications.NotificationMessageEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.portalblocks.component.ActivePortalComponent;
import org.terasology.portalblocks.component.BluePortalComponent;
import org.terasology.portalblocks.component.OrangePortalComponent;
import org.terasology.registry.In;
import org.terasology.speedboostblocks.component.SpeedBoostComponent;
import org.terasology.world.WorldProvider;

import java.util.List;

/**
 * This class manages and controls Breaking blocks.
 * <p>
 * <p>BreakingBlocks break after a while when you move over them.</p>
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class PortalSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(org.terasology.breakingblocks.BreakingSystem.class);

    private List<EntityRef> activatedPortalBlocks;

    private EntityRef activatedBluePortal, activatedOrangePortal;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private LocalPlayer localPlayer;

    @In
    private PrefabManager prefabManager;

    @In
    private org.terasology.engine.Time time;

    /**
     * This is called when the character is moving.
     *
     * @param moveInputEvent The details of the movement.
     * @param player         The player entity.
     * @param location       The player's location.
     */
    @ReceiveEvent(components = {LocationComponent.class, CharacterMovementComponent.class})
    public void onCharacterMove(CharacterMoveInputEvent moveInputEvent, EntityRef player, LocationComponent location) {


        EntityRef standingOnPortal, otherPortal;

        if (activatedPortalBlocks.size() != 2) {
            return;
        }

        // Get the player's location
        Vector3f playerWorldLocation = location.getWorldPosition();

        // If it doesn't exist
        if (playerWorldLocation == null) {
            // Something is very wrong so return
            return;
        }


        // Iterate through all the activated portal blocks
        for (int i = 0; i < 2; i++) {
            EntityRef entity = activatedPortalBlocks.get(i);

            // Get the block's location data
            LocationComponent entityLocation = entity.getComponent(LocationComponent.class);

            // Get the block's position
            Vector3f entityWorldLocation = entityLocation.getWorldPosition();

            // If the player's location matches the block's on the x and z axis (we round it to make the numbers usable)
            if (Math.round(playerWorldLocation.x) == entityWorldLocation.x && Math.round(playerWorldLocation.z) == entityWorldLocation.z) {

                // If the block is underneath the player
                if (Math.round(playerWorldLocation.y - 1) == entityWorldLocation.y) {
                    standingOnPortal = entity;
                    otherPortal = activatedPortalBlocks.get(i == 0 ? 1 : 0);
                    logger.info("Standing on activated Portal");
                }
                // We've found a block underneath us so there is no need to continue looking
                break;
            }
        }
    }

    @ReceiveEvent(components = {BluePortalComponent.class})
    public void onBluePortalActivate(ActivateEvent event, EntityRef entity) {

        if (entity.hasComponent(ActivePortalComponent.class)) {
            entity.send(new NotificationMessageEvent("This portal is already activated!", localPlayer.getCharacterEntity()));
            return;
        }

        if (activatedBluePortal != null)
            activatedBluePortal.removeComponent(ActivePortalComponent.class);

        entity.addComponent(new ActivePortalComponent());
        entity.send(new NotificationMessageEvent("Activated blue portal", localPlayer.getClientEntity()));
        activatedBluePortal = entity;
        updateActivatedPortals();
    }

    @ReceiveEvent(components = {OrangePortalComponent.class})
    public void onOrangePortalActivate(ActivateEvent event, EntityRef entity) {

        if (entity.hasComponent(ActivePortalComponent.class)) {
            entity.send(new NotificationMessageEvent("This portal is already activated!", localPlayer.getClientEntity()));
            return;
        }

        if (activatedOrangePortal != null)
            activatedOrangePortal.removeComponent(ActivePortalComponent.class);

        entity.addComponent(new ActivePortalComponent());
        entity.send(new NotificationMessageEvent("Activated orange portal", localPlayer.getClientEntity()));
        activatedOrangePortal = entity;
        updateActivatedPortals();
    }

    private void updateActivatedPortals() {
        activatedPortalBlocks = com.google.common.collect.Lists.newArrayList();
        activatedPortalBlocks.add(activatedBluePortal);
        activatedPortalBlocks.add(activatedOrangePortal);
    }

    @Override
    public void initialise(){
        activatedPortalBlocks = com.google.common.collect.Lists.newArrayList();
        activatedBluePortal = null;
        activatedOrangePortal = null;

        for (EntityRef entity : entityManager.getEntitiesWith(ActivePortalComponent.class)) {

            // Get the block's location data
            LocationComponent entityLocation = entity.getComponent(LocationComponent.class);

            // If it does not exist
            if (entityLocation == null) {
                // This block is not in the world so move onto the next one
                continue;
            }

            // Get the block's position
            Vector3f entityWorldLocation = entityLocation.getWorldPosition();

            // If it does not exist
            if (entityWorldLocation == null) {
                // Move on (this should never be called)
                continue;
            }

            activatedPortalBlocks.add(entity);
        }

        // Iterate through all the activated portal blocks
        for (int i = 0; i < activatedPortalBlocks.size(); i++) {
            EntityRef entity = activatedPortalBlocks.get(i);
            if (entity.hasComponent(BluePortalComponent.class)) {
                activatedBluePortal = entity;
            } else if (entity.hasComponent(OrangePortalComponent.class)) {
                activatedOrangePortal = entity;
            }
        }


    }

    @Override
    public void update(float delta) {

    }
}

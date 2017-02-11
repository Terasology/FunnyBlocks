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
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.notifications.NotificationMessageEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.portalblocks.component.ActivePortalComponent;
import org.terasology.portalblocks.component.BluePortalComponent;
import org.terasology.portalblocks.component.OrangePortalComponent;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.OnActivatedBlocks;
import org.terasology.world.chunks.event.OnChunkLoaded;

import java.util.List;

/**
 * This class manages and controls Portal blocks.
 * <p>Portal Blocks allow you to teleport from A to B.</p>
 * <p>There are two types of Portal blocks- blue and orange.
 * Only one of each can remain activated at once resulting in a discrete pathway.</p>
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class PortalSystem extends BaseComponentSystem{

    private static final Logger logger = LoggerFactory.getLogger(PortalSystem.class);

    private List<EntityRef> activatedPortalBlocks;

    private EntityRef activatedBluePortal, activatedOrangePortal;

    private EntityRef activatedPortals = EntityRef.NULL;

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
    public void onCharacterMove(VerticalCollisionEvent moveInputEvent, EntityRef player, LocationComponent location) {


        EntityRef otherPortal;

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
        for (int i = 0; i < activatedPortalBlocks.size(); i++) {
            EntityRef entity = activatedPortalBlocks.get(i);

            // Get the block's location data
            LocationComponent entityLocation = entity.getComponent(LocationComponent.class);

            // Get the block's position
            Vector3f entityWorldLocation = entityLocation.getWorldPosition();

            // If the player's location matches the block's on the x and z axis (we round it to make the numbers usable)
            if (Math.round(playerWorldLocation.x) == entityWorldLocation.x && Math.round(playerWorldLocation.z) == entityWorldLocation.z) {

                // If the block is underneath the player
                if (Math.round(playerWorldLocation.y - 1) == entityWorldLocation.y) {
                    // Get the other activated portal
                    otherPortal = activatedPortalBlocks.get(i == 0 ? 1 : 0);
                    // Teleport destination should be around the exit portal
                    // TODO: check if player can teleport i.e. blocks around are empty up to player height
                    Vector3f teleportDestination = otherPortal.getComponent(LocationComponent.class).getWorldPosition().add(1,1,1);
                    localPlayer.getCharacterEntity().send(new CharacterTeleportEvent(teleportDestination));
                }
                // We've found a block underneath us so there is no need to continue looking
                break;
            }
        }
    }

    /*
     * On 'e' press, interaction with BluePortalBlock
     */
    @ReceiveEvent(components = {BluePortalComponent.class})
    public void onBluePortalActivate(ActivateEvent event, EntityRef entity) {

        // If the block is already activated
        if (entity.hasComponent(ActivePortalComponent.class)) {
            localPlayer.getClientEntity().send(new NotificationMessageEvent("This portal is already activated. " + (activatedOrangePortal == null ? "Activate an Orange Portal to complete pathway." : "Jump on top to teleport!"), localPlayer.getClientEntity()));
            return;
        }

        // If there is a previously activated BluePortal, deactivate it
        if (activatedBluePortal != null)
            activatedBluePortal.removeComponent(ActivePortalComponent.class);

        entity.addComponent(new ActivePortalComponent());
        localPlayer.getClientEntity().send(new NotificationMessageEvent("Activated Blue Portal. " + (activatedOrangePortal == null ? "Activate an Orange Portal to complete pathway." : "Jump on top to teleport!"), localPlayer.getClientEntity()));
        activatedBluePortal = entity;
        updateActivatedPortals();
    }

    /*
     * On 'e' press, interaction with OrangePortalBlock
     */
    @ReceiveEvent(components = {OrangePortalComponent.class})
    public void onOrangePortalActivate(ActivateEvent event, EntityRef entity) {

        // If the block is already activated
        if (entity.hasComponent(ActivePortalComponent.class)) {
            localPlayer.getClientEntity().send(new NotificationMessageEvent("This portal is already activated. " + (activatedBluePortal == null ? "Activate a Blue Portal to complete pathway." : "Jump on top to teleport!"), localPlayer.getClientEntity()));
            return;
        }

        // If there is a previously activated OrangePortal, deactivate it
        if (activatedOrangePortal != null)
            activatedOrangePortal.removeComponent(ActivePortalComponent.class);

        entity.addComponent(new ActivePortalComponent());
        localPlayer.getClientEntity().send(new NotificationMessageEvent("Activated Orange Portal. " + (activatedBluePortal == null ? "Activate a Blue Portal to complete pathway." : "Jump on top to teleport!"), localPlayer.getClientEntity()));
        activatedOrangePortal = entity;
        updateActivatedPortals();
    }

    // Updates the activatedPortalBlocks list
    private void updateActivatedPortals() {
        activatedPortalBlocks = com.google.common.collect.Lists.newArrayList();
        if (activatedBluePortal != null)
            activatedPortalBlocks.add(activatedBluePortal);
        if (activatedOrangePortal != null)
            activatedPortalBlocks.add(activatedOrangePortal);
    }

    /*
     * This should fetch activated portal blocks when world generation is complete.
     * Activated portal block entities are registered as activatedBluePortal and activatedOrangePortal
     */
    @ReceiveEvent
    public void initialiseActivatedBlocks(OnChunkLoaded event, EntityRef entityRef) {
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
        logger.info("nihal111: Blue-" + (activatedBluePortal != null) + " Orange-" + (activatedOrangePortal != null));
    }

    @ReceiveEvent(components = ActivePortalComponent.class)
    public void onDestroy(DestroyEvent event, EntityRef entity) {
        if (entity.hasComponent(BluePortalComponent.class))
            activatedBluePortal = null;
        else
            activatedOrangePortal = null;
        updateActivatedPortals();
    }
}

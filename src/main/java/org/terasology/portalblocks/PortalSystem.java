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
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.notifications.NotificationMessageEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.portalblocks.component.ActivePortalComponent;
import org.terasology.portalblocks.component.ActivePortalPairComponent;
import org.terasology.portalblocks.component.BluePortalComponent;
import org.terasology.portalblocks.component.OrangePortalComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

/**
 * This class manages and controls Portal blocks.
 * <p>Portal Blocks allow you to teleport from A to B.</p>
 * <p>There are two types of Portal blocks- blue and orange.
 * Only one of each can remain activated at once resulting in a discrete pathway.</p>
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class PortalSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PortalSystem.class);

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

        ActivePortalPairComponent activePortalPairComponent = activatedPortals.getComponent(ActivePortalPairComponent.class);

        if (activePortalPairComponent.bluePortalLocation == null || activePortalPairComponent.orangePortalLocation == null) {
            return;
        }

        // Get the player's location
        Vector3f playerWorldLocation = location.getWorldPosition();

        // If it doesn't exist
        if (playerWorldLocation == null) {
            // Something is very wrong so return
            return;
        }

        Vector3f positionBlockUnder = worldProvider.getBlock(playerWorldLocation.addY(-1)).getEntity().getComponent(LocationComponent.class).getWorldPosition();
        if (activePortalPairComponent.bluePortalLocation == positionBlockUnder) {
            Vector3f teleportDestination = activePortalPairComponent.orangePortalLocation.add(1, 1, 1);
            localPlayer.getCharacterEntity().send(new CharacterTeleportEvent(teleportDestination));
        } else if (activePortalPairComponent.orangePortalLocation == positionBlockUnder) {
            Vector3f teleportDestination = activePortalPairComponent.bluePortalLocation.add(1, 1, 1);
            localPlayer.getCharacterEntity().send(new CharacterTeleportEvent(teleportDestination));
        }
    }

    /*
     * On 'e' press, interaction with BluePortalBlock
     */
    @ReceiveEvent(components = {BluePortalComponent.class})
    public void onBluePortalActivate(ActivateEvent event, EntityRef entity) {

        ActivePortalPairComponent activePortalPairComponent = activatedPortals.getComponent(ActivePortalPairComponent.class);
        boolean activatedOrangePortal = activePortalPairComponent.orangePortalLocation != null;
        boolean activatedBluePortal = activePortalPairComponent.bluePortalLocation != null;

        // If the block is already activated
        if (entity.hasComponent(ActivePortalComponent.class)) {
            localPlayer.getClientEntity().send(new NotificationMessageEvent("This portal is already activated. " + (!activatedOrangePortal ? "Activate an Orange Portal to complete pathway." : "Jump on top to teleport!"), localPlayer.getClientEntity()));
            return;
        }

        // If there is a previously activated BluePortal, deactivate it
        if (activatedBluePortal) {
            worldProvider.getBlock(activePortalPairComponent.bluePortalLocation).getEntity().removeComponent(ActivePortalComponent.class);
        }

        entity.addComponent(new ActivePortalComponent());
        localPlayer.getClientEntity().send(new NotificationMessageEvent("Activated Blue Portal. " + (!activatedOrangePortal ? "Activate an Orange Portal to complete pathway." : "Jump on top to teleport!"), localPlayer.getClientEntity()));

        activatedPortals.getComponent(ActivePortalPairComponent.class).bluePortalLocation = entity.getComponent(LocationComponent.class).getWorldPosition();
    }

    /*
     * On 'e' press, interaction with OrangePortalBlock
     */
    @ReceiveEvent(components = {OrangePortalComponent.class})
    public void onOrangePortalActivate(ActivateEvent event, EntityRef entity) {

        ActivePortalPairComponent activePortalPairComponent = activatedPortals.getComponent(ActivePortalPairComponent.class);
        boolean activatedOrangePortal = activePortalPairComponent.orangePortalLocation != null;
        boolean activatedBluePortal = activePortalPairComponent.bluePortalLocation != null;
        // If the block is already activated
        if (entity.hasComponent(ActivePortalComponent.class)) {
            localPlayer.getClientEntity().send(new NotificationMessageEvent("This portal is already activated. " + (!activatedBluePortal ? "Activate a Blue Portal to complete pathway." : "Jump on top to teleport!"), localPlayer.getClientEntity()));
            return;
        }

        // If there is a previously activated OrangePortal, deactivate it
        if (activatedOrangePortal)
            worldProvider.getBlock(activePortalPairComponent.orangePortalLocation).getEntity().removeComponent(ActivePortalComponent.class);

        entity.addComponent(new ActivePortalComponent());
        localPlayer.getClientEntity().send(new NotificationMessageEvent("Activated Orange Portal. " + (!activatedBluePortal ? "Activate a Blue Portal to complete pathway." : "Jump on top to teleport!"), localPlayer.getClientEntity()));

        activatedPortals.getComponent(ActivePortalPairComponent.class).orangePortalLocation = entity.getComponent(LocationComponent.class).getWorldPosition();
    }


    @Override
    public void postBegin() {

        if (!entityManager.getEntitiesWith(ActivePortalPairComponent.class).iterator().hasNext()) {
            logger.info("Swagger!");
            activatedPortals = entityManager.create();
            ActivePortalPairComponent activePortalPairComponent = new ActivePortalPairComponent();
            activePortalPairComponent.bluePortalLocation = null;
            activePortalPairComponent.orangePortalLocation = null;
            activatedPortals.addOrSaveComponent(activePortalPairComponent);
        } else {
            logger.info("Jagger!");

        }
    }

    @ReceiveEvent(components = ActivePortalComponent.class)
    public void onDestroy(DestroyEvent event, EntityRef entity) {
        if (entity.hasComponent(BluePortalComponent.class)) {
            activatedPortals.getComponent(ActivePortalPairComponent.class).bluePortalLocation = null;
        } else {
            activatedPortals.getComponent(ActivePortalPairComponent.class).orangePortalLocation = null;
        }
    }
}

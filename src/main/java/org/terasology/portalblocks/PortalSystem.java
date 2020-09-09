// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.portalblocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.destruction.DestroyEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.notifications.NotificationMessageEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.portalblocks.component.ActivePortalComponent;
import org.terasology.portalblocks.component.ActivePortalPairComponent;
import org.terasology.portalblocks.component.BluePortalComponent;
import org.terasology.portalblocks.component.OrangePortalComponent;

/**
 * This class manages and controls Portal blocks.
 * <p>Portal Blocks allow you to teleport from A to B.</p>
 * <p>There are two types of Portal blocks- blue and orange.
 * Only one of each can remain activated at once resulting in a discrete pathway.</p>
 */

@RegisterSystem
public class PortalSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PortalSystem.class);

    // Global entity which contains ActivePortalPairComponent and has locations of activated Portals
    private EntityRef activatedPortals = EntityRef.NULL;
    private Vector3i oldPosition;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityProvider;

    @In
    private PrefabManager prefabManager;

    @In
    private org.terasology.engine.core.Time time;

    /**
     * This is called when the character is moving.
     *
     * @param moveInputEvent The details of the movement.
     * @param player The player entity.
     * @param location The player's location.
     */
    @ReceiveEvent(components = {LocationComponent.class, CharacterMovementComponent.class})
    public void onCharacterMove(CharacterMoveInputEvent moveInputEvent, EntityRef player, LocationComponent location) {

        // Get the player's location
        Vector3f playerWorldLocation = location.getWorldPosition();

        // If it doesn't exist
        if (playerWorldLocation == null) {
            // Something is very wrong so return
            return;
        }

        // Check if the block under the player is a portal block and initiate teleportation
        Vector3i roundedPosition = new Vector3i(Math.round(playerWorldLocation.x), Math.round(playerWorldLocation.y),
                Math.round(playerWorldLocation.z));

        // continue only if there is a change in player position
        if (oldPosition != null && !roundedPosition.equals(oldPosition)) {
            oldPosition = roundedPosition;
        } else {
            oldPosition = roundedPosition;
            return;
        }

        ActivePortalPairComponent activePortalPairComponent =
                activatedPortals.getComponent(ActivePortalPairComponent.class);

        if (activePortalPairComponent.bluePortalLocation == null || activePortalPairComponent.orangePortalLocation == null) {
            return;
        }

        // Check if the block under the player is a portal block and initiate teleportation
        Vector3i positionBlockUnder = new Vector3i(0, -1, 0).add(roundedPosition);
        if (activePortalPairComponent.bluePortalLocation.equals(positionBlockUnder)) {
            teleport(activePortalPairComponent.orangePortalLocation.toVector3f(), player);
        } else if (activePortalPairComponent.orangePortalLocation.equals(positionBlockUnder)) {
            teleport(activePortalPairComponent.bluePortalLocation.toVector3f(), player);
        }
    }

    private void teleport(Vector3f destination, EntityRef character) {
        float playerHeight = character.getComponent(CharacterMovementComponent.class).height;
        int j;
        for (int i = -1; i <= 1; i++) {
            for (int k = -1; k <= 1; k++) {
                if (!(i == 0 && k == 0)) {
                    Vector3f finalDestination = new Vector3f(i, 0, k).add(destination);
                    Block block = worldProvider.getBlock(finalDestination);
                    if (block.isPenetrable()) {
                        for (j = 0; j < playerHeight; j++) {
                            Vector3f heightCheck = new Vector3f(0, j, 0).add(finalDestination);
                            Block heightBlock = worldProvider.getBlock(heightCheck);
                            if (!heightBlock.isPenetrable()) {
                                break;
                            }
                        }
                        if (j == Math.ceil(playerHeight)) {
                            character.send(new CharacterTeleportEvent(finalDestination.addY(1)));
                            return;
                        }
                    }

                }
            }
        }
        character.getOwner().send(new NotificationMessageEvent("Could not teleport to the other portal as there is no" +
                " space around it!", character));
    }

    /*
     * On 'e' press, interaction with BluePortalBlock
     */
    @ReceiveEvent(components = {BluePortalComponent.class})
    public void onBluePortalActivate(ActivateEvent event, EntityRef entity) {
        ActivePortalPairComponent activePortalPairComponent =
                activatedPortals.getComponent(ActivePortalPairComponent.class);
        boolean activatedOrangePortal = activePortalPairComponent.orangePortalLocation != null;
        boolean activatedBluePortal = activePortalPairComponent.bluePortalLocation != null;
        EntityRef client = event.getInstigator().getOwner();

        // If the block is already activated, do nothing
        if (entity.hasComponent(ActivePortalComponent.class)) {
            client.send(new NotificationMessageEvent("This portal is already activated. " + (!activatedOrangePortal ?
                    "Activate an Orange Portal to complete pathway." : "Jump on top to teleport!"), client));
            return;
        }

        // If there is a previously activated BluePortal, deactivate it and activate the new one
        if (activatedBluePortal) {
            worldProvider.getBlock(activePortalPairComponent.bluePortalLocation).getEntity().removeComponent(ActivePortalComponent.class);
        }

        entity.addComponent(new ActivePortalComponent());
        client.send(new NotificationMessageEvent("Activated Blue Portal. " + (!activatedOrangePortal ? "Activate an " +
                "Orange Portal to complete pathway." : "Jump on top to teleport!"), client));

        // Update activatedPortals entity to store the new location of bluePortalBlock
        activePortalPairComponent.bluePortalLocation = entity.getComponent(BlockComponent.class).getPosition();
        activatedPortals.addOrSaveComponent(activePortalPairComponent);
    }

    /*
     * On 'e' press, interaction with OrangePortalBlock
     */
    @ReceiveEvent(components = {OrangePortalComponent.class})
    public void onOrangePortalActivate(ActivateEvent event, EntityRef entity) {
        ActivePortalPairComponent activePortalPairComponent =
                activatedPortals.getComponent(ActivePortalPairComponent.class);
        boolean activatedOrangePortal = activePortalPairComponent.orangePortalLocation != null;
        boolean activatedBluePortal = activePortalPairComponent.bluePortalLocation != null;
        EntityRef client = event.getInstigator().getOwner();

        // If the block is already activated, do nothing
        if (entity.hasComponent(ActivePortalComponent.class)) {
            client.send(new NotificationMessageEvent("This portal is already activated. " + (!activatedBluePortal ? 
                    "Activate a Blue Portal to complete pathway." : "Jump on top to teleport!"), client));
            return;
        }

        // If there is a previously activated OrangePortal, deactivate it and activate the new one
        if (activatedOrangePortal)
            worldProvider.getBlock(activePortalPairComponent.orangePortalLocation).getEntity().removeComponent(ActivePortalComponent.class);

        entity.addComponent(new ActivePortalComponent());
        client.send(new NotificationMessageEvent("Activated Orange Portal. " + (!activatedBluePortal ? "Activate a " +
                "Blue Portal to complete pathway." : "Jump on top to teleport!"), client));

        // Update activatedPortals entity to store the new location of orangePortalBlock
        activePortalPairComponent.orangePortalLocation = entity.getComponent(BlockComponent.class).getPosition();
        activatedPortals.addOrSaveComponent(activePortalPairComponent);
    }


    @Override
    public void postBegin() {

        // On startup initialise/fetch the activatedPortals entity to contain the portal locations
        if (!entityManager.getEntitiesWith(ActivePortalPairComponent.class).iterator().hasNext()) {
            // This happens only when a new game is created.
            activatedPortals = entityManager.create();
            ActivePortalPairComponent activePortalPairComponent = new ActivePortalPairComponent();
            activePortalPairComponent.bluePortalLocation = null;
            activePortalPairComponent.orangePortalLocation = null;
            activatedPortals.addOrSaveComponent(activePortalPairComponent);
        } else {
            // This happens on loading a saved game, when the activatedPortal entity already exists
            activatedPortals = entityManager.getEntitiesWith(ActivePortalPairComponent.class).iterator().next();
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

/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:// www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.breakingblocks;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.breakingblocks.component.BreakingComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterImpulseEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.*;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.In;
import org.terasology.speedboostblocks.component.SpeedBoostComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;

import java.util.List;

/**
 * This class manages and controls Breaking blocks.
 * <p>
 * <p>BreakingBlocks break after a while when you move over them.</p>
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class BreakingSystem extends BaseComponentSystem implements UpdateSubscriberSystem{
    private static final Logger logger = LoggerFactory.getLogger(BreakingSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

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
    @ReceiveEvent(components = {LocationComponent.class})
    public void onCharacterMovement(CharacterMoveInputEvent moveInputEvent, EntityRef player, LocationComponent location) {

        // Get the player's location
        Vector3f playerWorldLocation = location.getWorldPosition();

        // If it doesn't exist
        if (playerWorldLocation == null) {
            // Something is very wrong so return
            return;
        }

        // Iterate through all the Breaking blocks
        for (EntityRef entity : entityManager.getEntitiesWith(BreakingComponent.class)) {
            // Get the block's location data
            LocationComponent entityLocation = entity.getComponent(LocationComponent.class);
            BlockComponent blockComponent = entity.getComponent(BlockComponent.class);

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

            // If the player's location matches the block's on the x and z axis (we round it to make the numbers usable)
            if (Math.round(playerWorldLocation.x) == entityWorldLocation.x && Math.round(playerWorldLocation.z) == entityWorldLocation.z) {

                // If the block is underneath the player
                if (Math.round(playerWorldLocation.y - 1) == entityWorldLocation.y) {

                    // Get the Breaking block's properties
                    BreakingComponent breakingComponent = entity.getComponent(BreakingComponent.class);

                    if (!breakingComponent.triggered) {
                        breakingComponent.triggered = true;
                        breakingComponent.breakTime = time.getGameTimeInMs() + TeraMath.floorToInt(breakingComponent.breakInterval * 1000);
                        entity.saveComponent(breakingComponent);
                        logger.info("TRIGGERED");
                    }
                    // We've found a block underneath us so there is no need to continue looking
                    break;
                }
            }
        }

    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(BreakingComponent.class)) {
            BreakingComponent breakingComponent = entity.getComponent(BreakingComponent.class);
            long breakTime = breakingComponent.breakTime;
            if (time.getGameTimeInMs() >= breakTime && breakingComponent.triggered) {
                logger.info("damaging");
                breakingComponent.breakTime = time.getGameTimeInMs() + TeraMath.floorToInt(breakingComponent.breakInterval * 1000);
                entity.saveComponent(breakingComponent);
                // Is PHYSICAL the correct damage type?
                entity.send(new DoDamageEvent(1, EngineDamageTypes.PHYSICAL.get()));
            }
        }
    }
}

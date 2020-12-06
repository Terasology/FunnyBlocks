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
package org.terasology.funnyblocks.system;

import org.joml.Vector3f;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.logic.characters.CharacterImpulseEvent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.entitySystem.systems.RegisterMode;

import org.terasology.funnyblocks.component.BouncyBlockComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BouncySystem extends BaseComponentSystem {
    @In
    private BlockEntityRegistry blockEntityProvider;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    private final Logger logger = LoggerFactory.getLogger(BouncySystem.class);

    @ReceiveEvent
    public void onCharacterMovement(CharacterMoveInputEvent event, EntityRef player) {
        if (player.getComponent(LocationComponent.class) == null || player.getComponent(LocationComponent.class).getWorldPosition() == null) {
            return;
        }

        Vector3f playerPosition = player.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        for (EntityRef blockEntity : entityManager.getEntitiesWith(BouncyBlockComponent.class, LocationComponent.class)) {
            Vector3f blockPos = blockEntity.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
            if (blockPos != null && playerPosition != null) {
                if (Math.round(playerPosition.x) == blockPos.x && Math.round(playerPosition.z) == blockPos.z
                        && Math.round(playerPosition.y - 1) == blockPos.y) {
                    Vector3f impulse = new Vector3f(0, blockEntity.getComponent(BouncyBlockComponent.class).force, 0);
                    player.send(new CharacterImpulseEvent(impulse));
                }
            }
        }
    }
}

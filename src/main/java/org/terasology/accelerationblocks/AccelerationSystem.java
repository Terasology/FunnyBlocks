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
package org.terasology.accelerationblocks;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.accelerationblocks.component.AccelerationBlockComponent;
import org.terasology.accelerationblocks.component.AccelerationComponent;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.CharacterImpulseEvent;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.characters.events.OnEnterBlockEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;

/**
 * This system reacts on Entities passing through block with AccelerationBlockComponent, adding AccelerationComponent to them
 * All Entities with AccelerationComponent get acceleration
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class AccelerationSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(AccelerationSystem.class);

    @In
    private BlockEntityRegistry blockEntityProvider;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(AccelerationComponent.class, LocationComponent.class)) {
            AccelerationComponent acceleration = entity.getComponent(AccelerationComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            applyImpulse(acceleration, entity);
        }
    }

    private void applyImpulse(AccelerationComponent acceleration, EntityRef entity) {
        Vector3f impulse = new Vector3f(acceleration.velocity);
        entity.send(new CharacterImpulseEvent(impulse));
    }

    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        //ignores "flying" block
        if (isAtHeadLevel(event.getCharacterRelativePosition(), entity)) {
            return;
        }


        Block block = event.getNewBlock();
        if (blockIsAccelerating(block)) {
            AccelerationBlockComponent blockAcceleration = block.getEntity().getComponent(AccelerationBlockComponent.class);
            AccelerationComponent acceleration = entity.getComponent(AccelerationComponent.class);

            if (acceleration == null) {
                acceleration = new AccelerationComponent();
                initAcceleration(acceleration, blockAcceleration, block.getDirection());
                entity.addComponent(acceleration);
            } else {
                initAcceleration(acceleration, blockAcceleration, block.getDirection());
                entity.saveComponent(acceleration);
            }
        } else {
            //check if it was accelerated before and removes acceleration component
            AccelerationComponent accelerationOld = entity.getComponent(AccelerationComponent.class);

            if (accelerationOld != null) {
                entity.removeComponent(AccelerationComponent.class);
            }
        }
    }

    private void initAcceleration(AccelerationComponent acceleration, AccelerationBlockComponent blockAcceleration, Side side) {
        Vector3f velocity = new Vector3f(blockAcceleration.velocity);
        if (!blockAcceleration.ignoreBlockDirection) {
            Vector3ic originalDirection = Side.FRONT.direction();
            Vector3ic blockDirection = side.direction();
            double angle = Math.acos(originalDirection.x() * blockDirection.x() + originalDirection.z() * blockDirection.z());

            Vector3i rotAxis = new Vector3i(0,1,0);
            Quaternionf rotation = getRotationQuat(rotAxis.x(), rotAxis.y(), rotAxis.z(), (float) angle);
            Quaternionf temp = new Quaternionf(rotation);
            temp.transform(velocity);
            temp.invert(rotation);

            //to determine right and left directions
            if (blockDirection.x() > 0) {
                temp.invert();
            }

            velocity.x = temp.x();
            velocity.y = temp.y();
            velocity.z = temp.z();
        }

        acceleration.velocity = velocity;
        acceleration.ignoreBlockDirection = blockAcceleration.ignoreBlockDirection;
    }

    /**
     * Generates rotation Quat
     * @param x Rotation axis x
     * @param y Rotation axis y
     * @param z Rotation axis z
     * @param angle Rotation angle
     */
    private static Quaternionf getRotationQuat(int x, int y, int z, float angle) {
        Quaternionf result = new Quaternionf();
        float nw = (float) Math.cos(angle / 2);
        float nx = x * (float) Math.sin(angle / 2);
        float ny = y * (float) Math.sin(angle / 2);
        float nz = z * (float) Math.sin(angle / 2);
        result.set(nx, ny, nz, nw);
        return result;
    }

    private boolean isAtHeadLevel(Vector3i relativePosition, EntityRef entity) {
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        return (int) Math.ceil(characterMovementComponent.height) - 1 == relativePosition.y;
    }

    private boolean blockIsAccelerating(Block block) {
        return block.getEntity().hasComponent(AccelerationBlockComponent.class);
    }
}

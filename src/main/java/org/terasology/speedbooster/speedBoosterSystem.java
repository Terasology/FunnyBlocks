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

package org.terasology.speedbooster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterImpulseEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

/**
 * Created by ElBatanony on 30-Nov-16.
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class speedBoosterSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(speedBoosterSystem.class);

    @In
    private BlockEntityRegistry blockEntityProvider;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(CharacterMovementComponent.class, LocationComponent.class)) {

            LocationComponent loc = entity.getComponent(LocationComponent.class);
            Vector3f pos = loc.getWorldPosition();
            //EntityRef underBlock = blockEntityProvider.getBlockEntityAt(loc.getWorldPosition());
            pos.setY( pos.getY() -1 );
            Block block = worldProvider.getBlock(pos);

            CharacterMovementComponent cmc = entity.getComponent( CharacterMovementComponent.class );
            if ( block.getDisplayName().equals( "Speedblock" ) ) {
                cmc.speedMultiplier = 5f;
                //logger.info("speedy :D");
                entity.saveComponent(cmc);
                Vector3f imp = cmc.getVelocity().normalize().scale(64).setY(6);
                entity.send(new CharacterImpulseEvent(imp));
            } else {
                cmc.speedMultiplier = 1f;
                //logger.info("not speedy :(");
                entity.saveComponent(cmc);
            }

        }
    }



}


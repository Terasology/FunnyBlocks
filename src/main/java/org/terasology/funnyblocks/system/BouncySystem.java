// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.funnyblocks.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterImpulseEvent;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.funnyblocks.component.BouncyBlockComponent;
import org.terasology.math.geom.Vector3f;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BouncySystem extends BaseComponentSystem {
    private final Logger logger = LoggerFactory.getLogger(BouncySystem.class);
    @In
    private BlockEntityRegistry blockEntityProvider;
    @In
    private Time time;
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void onCharacterMovement(CharacterMoveInputEvent event, EntityRef player) {
        if (player.getComponent(LocationComponent.class) == null || player.getComponent(LocationComponent.class).getWorldPosition() == null) {
            return;
        }

        Vector3f playerPosition = new Vector3f(player.getComponent(LocationComponent.class).getWorldPosition());
        for (EntityRef blockEntity : entityManager.getEntitiesWith(BouncyBlockComponent.class,
                LocationComponent.class)) {
            Vector3f blockPos = blockEntity.getComponent(LocationComponent.class).getWorldPosition();
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

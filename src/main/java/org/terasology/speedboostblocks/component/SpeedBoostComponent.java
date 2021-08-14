// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.speedboostblocks.component;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This component holds the data for a SpeedBoost block.
 */
public class SpeedBoostComponent implements Component<SpeedBoostComponent> {
    public int speedIncrease = 2;

    /**
     * UP = 0
     * RIGHT = 1
     * DOWN = 2
     * LEFT = 3
     * PLAYER DIRECTION = 4
     */
    public int boostDirection = 0;

    /**
     * This changes the block's direction
     */
    public void changeDirection() {
        //Move the direction clockwise
        boostDirection++;

        //If it was facing left before the change then set it to up
        if(boostDirection > 3) {
            boostDirection = 0;
        }
    }

    @Override
    public void copyFrom(SpeedBoostComponent other) {
        this.boostDirection = other.boostDirection;
    }
}

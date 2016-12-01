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
package org.terasology.speedboostblocks.component;

import org.terasology.entitySystem.Component;

/**
 * This component holds the data for a SpeedBoost block.
 */
public class SpeedBoostComponent implements Component {
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
}

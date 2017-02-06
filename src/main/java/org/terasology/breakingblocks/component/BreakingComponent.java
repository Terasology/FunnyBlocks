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
package org.terasology.breakingblocks.component;

import org.terasology.entitySystem.Component;

/**
 * This component holds the data for a Breaking block.
 */

public class BreakingComponent implements Component {

    // Time interval between two damages of magnitude = 1
    public float breakInterval = 1.0f;

    // To store time when next damage will be inflicted
    public long breakTime = 0;

    // Stores whether block is walked over
    public boolean triggered = false;
}

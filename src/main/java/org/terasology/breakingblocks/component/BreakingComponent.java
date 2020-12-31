// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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

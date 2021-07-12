// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.portalblocks.component;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This component holds the data for a Breaking block.
 */

@Replicate
public class BluePortalComponent implements Component<BluePortalComponent> {

    // Stores whether block is activated
    public boolean activated = false;

    @Override
    public void copy(BluePortalComponent other) {
        this.activated = other.activated;
    }
}

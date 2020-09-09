// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.portalblocks.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

/**
 * This component holds the data for a Breaking block.
 */

@Replicate
public class BluePortalComponent implements Component {

    // Stores whether block is activated
    public boolean activated = false;
}

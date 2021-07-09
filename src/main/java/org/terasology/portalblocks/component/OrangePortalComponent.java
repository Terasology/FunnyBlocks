// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.portalblocks.component;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

@Replicate
public class OrangePortalComponent implements Component<OrangePortalComponent> {

    // Stores whether block is activated
    public boolean activated = false;
}

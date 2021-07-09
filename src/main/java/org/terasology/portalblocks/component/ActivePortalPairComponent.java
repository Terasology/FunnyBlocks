// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.portalblocks.component;

import org.joml.Vector3i;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

@Replicate
public class ActivePortalPairComponent implements Component<ActivePortalPairComponent> {
    public Vector3i orangePortalLocation;
    public Vector3i bluePortalLocation;
}

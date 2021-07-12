// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.portalblocks.component;

import org.joml.Vector3i;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

@Replicate
public class ActivePortalPairComponent implements Component<ActivePortalPairComponent> {
    public Vector3i orangePortalLocation = new Vector3i();
    public Vector3i bluePortalLocation = new Vector3i();

    @Override
    public void copy(ActivePortalPairComponent other) {
        this.orangePortalLocation.set(other.orangePortalLocation);
        this.bluePortalLocation.set(other.bluePortalLocation);
    }
}

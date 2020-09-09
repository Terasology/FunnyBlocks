// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.portalblocks.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.math.geom.Vector3i;

@Replicate
public class ActivePortalPairComponent implements Component {
    public Vector3i orangePortalLocation;
    public Vector3i bluePortalLocation;
}

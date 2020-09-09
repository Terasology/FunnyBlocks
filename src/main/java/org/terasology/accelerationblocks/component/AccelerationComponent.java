// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.accelerationblocks.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.Vector3f;

public final class AccelerationComponent implements Component {
    public Vector3f velocity;

    public boolean ignoreBlockDirection;
}

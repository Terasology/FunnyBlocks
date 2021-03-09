// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.accelerationblocks.component;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Component;

public final class AccelerationBlockComponent implements Component {
    public Vector3f velocity;

    public boolean ignoreBlockDirection;
}

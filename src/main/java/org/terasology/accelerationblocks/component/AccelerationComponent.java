// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.accelerationblocks.component;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.component.Component;

public final class AccelerationComponent implements Component<AccelerationComponent> {
    public Vector3f velocity = new Vector3f();
    public boolean ignoreBlockDirection;

    @Override
    public void copyFrom(AccelerationComponent other) {
        this.velocity.set(other.velocity);
        this.ignoreBlockDirection = other.ignoreBlockDirection;
        
    }
}

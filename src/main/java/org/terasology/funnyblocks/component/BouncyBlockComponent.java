// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.funnyblocks.component;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Bouncy block component, making block bouncy.
 */
public class BouncyBlockComponent implements Component<BouncyBlockComponent> {
    public float force = 20;
}

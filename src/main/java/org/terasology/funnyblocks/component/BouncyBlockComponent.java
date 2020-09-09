// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.funnyblocks.component;

import org.terasology.engine.entitySystem.Component;

/**
 * Bouncy block component, making block bouncy.
 */
public class BouncyBlockComponent implements Component {
    public float force = 20;
}

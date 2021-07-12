// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.portalblocks.component;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

@Replicate
public class ActivePortalComponent implements Component<ActivePortalComponent>{
    @Override
    public void copy(ActivePortalComponent other) {

    }
}

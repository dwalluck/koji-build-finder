/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.build.finder.core;

import java.io.Serial;
import java.io.Serializable;

public class BuildCheckedEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 8207145169396750092L;

    private final Checksum checksum;

    private final BuildSystem buildSystem;

    public BuildCheckedEvent(Checksum checksum, BuildSystem buildSystem) {
        this.checksum = checksum;
        this.buildSystem = buildSystem;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public BuildSystem getBuildSystem() {
        return buildSystem;
    }
}

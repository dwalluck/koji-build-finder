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
package com.redhat.red.build.finder;

import java.util.List;

import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;

public class KojiLocalArchive {
    private KojiArchiveInfo archive;

    private List<String> files;

    public KojiLocalArchive() {

    }

    public KojiLocalArchive(KojiArchiveInfo archive, List<String> files) {
        this.archive = archive;
        this.files = files;
    }

    public KojiArchiveInfo getArchive() {
        return archive;
    }

    public void setArchive(KojiArchiveInfo archive) {
        this.archive = archive;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    @Override
    public int hashCode() {
        return 31 + ((archive == null) ? 0 : archive.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        KojiLocalArchive other = (KojiLocalArchive) obj;

        if (archive == null) {
            if (other.archive != null) {
                return false;
            }
        } else if (!archive.equals(other.archive)) {
            return false;
        }

        return true;
    }
}

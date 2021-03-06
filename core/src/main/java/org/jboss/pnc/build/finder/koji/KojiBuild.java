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
package org.jboss.pnc.build.finder.koji;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.infinispan.commons.util.Util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.redhat.red.build.koji.model.json.KojiJsonConstants;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildRequest;
import com.redhat.red.build.koji.model.xmlrpc.KojiRpmInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTagInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskRequest;

@SerializeWith(KojiBuild.KojiBuildExternalizer.class)
public class KojiBuild {
    public static final String KEY_VERSION = "version";

    public static final String KEY_MAVEN = "maven";

    private KojiBuildInfo buildInfo;

    private KojiTaskInfo taskInfo;

    private transient KojiTaskRequest taskRequest;

    private transient List<KojiLocalArchive> archives;

    private List<KojiArchiveInfo> remoteArchives;

    private List<KojiTagInfo> tags;

    private transient List<String> types;

    private List<KojiRpmInfo> remoteRpms;

    private transient List<KojiArchiveInfo> duplicateArchives;

    public KojiBuild() {
        this.archives = new ArrayList<>();
        this.duplicateArchives = new ArrayList<>();
    }

    public KojiBuild(KojiBuildInfo buildInfo) {
        this.buildInfo = buildInfo;
        this.archives = new ArrayList<>();
        this.duplicateArchives = new ArrayList<>();
    }

    public KojiBuild(
            KojiBuildInfo buildInfo,
            KojiTaskInfo taskInfo,
            KojiTaskRequest taskRequest,
            List<KojiLocalArchive> archives,
            List<KojiArchiveInfo> remoteArchives,
            List<KojiTagInfo> tags,
            List<String> types,
            List<KojiRpmInfo> remoteRpms) {
        this.buildInfo = buildInfo;
        this.taskInfo = taskInfo;
        this.taskRequest = taskRequest;
        this.archives = archives;
        this.remoteArchives = remoteArchives;
        this.tags = tags;
        this.types = types;
        this.remoteRpms = remoteRpms;
    }

    @JsonIgnore
    public int getId() {
        if (buildInfo == null) {
            return -1;
        }

        return buildInfo.getId();
    }

    public KojiBuildInfo getBuildInfo() {
        return buildInfo;
    }

    public void setBuildInfo(KojiBuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }

    public KojiTaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(KojiTaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public KojiTaskRequest getTaskRequest() {
        if (taskRequest == null && taskInfo != null && taskInfo.getRequest() != null) {
            taskRequest = new KojiTaskRequest(taskInfo.getRequest());
        }

        return taskRequest;
    }

    public void setTaskRequest(KojiTaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }

    public List<KojiLocalArchive> getArchives() {
        return archives;
    }

    public void setArchives(List<KojiLocalArchive> archives) {
        this.archives = archives;
    }

    public List<KojiArchiveInfo> getRemoteArchives() {
        return remoteArchives;
    }

    public void setRemoteArchives(List<KojiArchiveInfo> remoteArchives) {
        this.remoteArchives = remoteArchives;
    }

    public List<KojiTagInfo> getTags() {
        return tags;
    }

    public void setTags(List<KojiTagInfo> tags) {
        this.tags = tags;
    }

    public List<String> getTypes() {
        if (types == null && buildInfo != null && buildInfo.getTypeNames() != null) {
            types = buildInfo.getTypeNames();
        }

        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<KojiRpmInfo> getRemoteRpms() {
        return remoteRpms;
    }

    public void setRemoteRpms(List<KojiRpmInfo> remoteRpms) {
        this.remoteRpms = remoteRpms;
    }

    public List<KojiArchiveInfo> getDuplicateArchives() {
        return duplicateArchives;
    }

    public void setDuplicateArchives(List<KojiArchiveInfo> duplicateArchives) {
        this.duplicateArchives = duplicateArchives;
    }

    @JsonIgnore
    public boolean isPnc() {
        return buildInfo != null && buildInfo.getExtra() != null
                && "PNC".equals(buildInfo.getExtra().get(KojiJsonConstants.BUILD_SYSTEM));
    }

    @JsonIgnore
    public Optional<KojiArchiveInfo> getProjectSourcesTgz() {
        String mavenArtifactId = buildInfo.getMavenArtifactId();
        String mavenVersion = buildInfo.getMavenVersion();

        if (remoteArchives != null && mavenArtifactId != null && mavenVersion != null) {
            String sourcesZipFilename = mavenArtifactId + "-" + mavenVersion + "-project-sources.tar.gz";
            return remoteArchives.stream()
                    .filter(sArchive -> sArchive.getFilename().equals(sourcesZipFilename))
                    .findFirst();
        }

        return Optional.empty();
    }

    @JsonIgnore
    public Optional<KojiArchiveInfo> getScmSourcesZip() {
        String mavenArtifactId = buildInfo.getMavenArtifactId();
        String mavenVersion = buildInfo.getMavenVersion();

        if (remoteArchives != null && mavenArtifactId != null && mavenVersion != null) {
            String sourcesZipFilename = mavenArtifactId + "-" + mavenVersion + "-scm-sources.zip";
            return remoteArchives.stream()
                    .filter(sArchive -> sArchive.getFilename().equals(sourcesZipFilename))
                    .findFirst();
        }

        return Optional.empty();
    }

    @JsonIgnore
    public Optional<KojiArchiveInfo> getPatchesZip() {
        String mavenArtifactId = buildInfo.getMavenArtifactId();
        String mavenVersion = buildInfo.getMavenVersion();

        if (remoteArchives != null && mavenArtifactId != null && mavenVersion != null) {
            String patchesZipFilename = mavenArtifactId + "-" + mavenVersion + "-patches.zip";
            return remoteArchives.stream()
                    .filter(pArchive -> pArchive.getFilename().equals(patchesZipFilename))
                    .findFirst();
        }

        return Optional.empty();
    }

    @JsonIgnore
    public boolean isImport() {
        return !(buildInfo != null && buildInfo.getExtra() != null
                && buildInfo.getExtra().containsKey(KojiJsonConstants.BUILD_SYSTEM) || taskInfo != null);
    }

    @JsonIgnore
    public boolean isMaven() {
        return buildInfo != null && buildInfo.getExtra() != null
                && (buildInfo.getExtra().containsKey(KEY_MAVEN)
                        || buildInfo.getExtra().get(KojiJsonConstants.BUILD_SYSTEM) != null
                                && buildInfo.getExtra().get(KojiJsonConstants.BUILD_SYSTEM).equals("PNC"))
                || taskInfo != null && taskInfo.getMethod() != null && taskInfo.getMethod().equals(KEY_MAVEN);
    }

    @JsonIgnore
    public Optional<String> getSource() {
        String source = null;

        if (buildInfo != null) {
            source = buildInfo.getSource();
        }

        if (source == null && getTaskRequest() != null) {
            KojiBuildRequest buildRequest = taskRequest.asBuildRequest();

            if (buildRequest != null) {
                source = buildRequest.getSource();
            }
        }

        return Optional.ofNullable(source);
    }

    @JsonIgnore
    public Optional<String> getMethod() {
        if (taskInfo != null) {
            String method = taskInfo.getMethod();

            if (method != null) {
                return Optional.of(method);
            }
        }

        if (buildInfo != null) {
            Map<String, Object> extra = buildInfo.getExtra();

            if (extra != null) {
                String buildSystem = (String) extra.get(KojiJsonConstants.BUILD_SYSTEM);

                if (buildSystem != null) {
                    String version = (String) extra.get(KEY_VERSION);

                    if (version != null) {
                        buildSystem += " " + version;
                    }

                    return Optional.of(buildSystem);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return "KojiBuild [buildInfo=" + buildInfo + ", taskInfo=" + taskInfo + ", taskRequest=" + taskRequest
                + ", archives=" + archives + ", remoteArchives=" + remoteArchives + ", tags=" + tags + ", remoteRpms="
                + remoteRpms + ", duplicateArchives=" + duplicateArchives + "]";
    }

    public static class KojiBuildExternalizer implements AdvancedExternalizer<KojiBuild> {
        private static final long serialVersionUID = 8698588352614405297L;

        private static final int VERSION = 2;

        private static final Integer ID = (Character.getNumericValue('K') << 16) | (Character.getNumericValue('B') << 8)
                | Character.getNumericValue('F');

        @Override
        public void writeObject(ObjectOutput output, KojiBuild object) throws IOException {
            output.writeInt(VERSION);
            output.writeObject(object.getBuildInfo());
            output.writeObject(object.getTaskInfo());
            output.writeObject(object.getRemoteArchives());
            output.writeObject(object.getTags());
            output.writeObject(object.getTypes());
            output.writeObject(object.getRemoteRpms());
        }

        @SuppressWarnings("unchecked")
        @Override
        public KojiBuild readObject(ObjectInput input) throws IOException, ClassNotFoundException {
            int version = input.readInt();

            if (version != 1 && version != 2) {
                throw new IOException("Invalid version: " + version);
            }

            KojiBuild build = new KojiBuild();

            build.setBuildInfo((KojiBuildInfo) input.readObject());
            build.setTaskInfo((KojiTaskInfo) input.readObject());
            build.setRemoteArchives((List<KojiArchiveInfo>) input.readObject());
            build.setTags((List<KojiTagInfo>) input.readObject());
            build.setTypes((List<String>) input.readObject());

            if (version > 1) {
                build.setRemoteRpms((List<KojiRpmInfo>) input.readObject());
            }

            return build;
        }

        @Override
        public Set<Class<? extends KojiBuild>> getTypeClasses() {
            return Util.asSet(KojiBuild.class);
        }

        @Override
        public Integer getId() {
            return ID;
        }
    }
}

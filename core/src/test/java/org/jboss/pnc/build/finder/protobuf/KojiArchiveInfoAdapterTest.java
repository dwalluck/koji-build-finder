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
package org.jboss.pnc.build.finder.protobuf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;

class KojiArchiveInfoAdapterTest {
    private static final EasyRandom EASY_RANDOM = new EasyRandom();

    @Test
    public void testSerializeDeserializeKojiArchiveInfo() {
        KojiArchiveInfo kojiArchiveInfo = EASY_RANDOM.nextObject(KojiArchiveInfo.class);
        KojiArchiveInfoAdapter adapter = new KojiArchiveInfoAdapter();
        String json = adapter.getJsonData(kojiArchiveInfo);
        KojiArchiveInfo deSerialized = adapter.create(json);

        assertEquals(kojiArchiveInfo.getArchiveId(), deSerialized.getArchiveId());
    }
}
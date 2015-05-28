/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.upgrade;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentVersionUpgrader;
import com.dreameddeath.core.model.IVersionedDocument;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Christophe Jeunesse on 28/11/2014.
 */
public class UpgraderTest {
    @DocumentDef(domain="test",name="test",version = "1.0.0")
    public static class TestModel implements IVersionedDocument {
        private String _versionnedDocument;
        @Override
        public void setDocumentFullVersionId(String versionId) {
            _versionnedDocument=versionId;
        }

        @Override
        public String getDocumentFullVersionId() {
            return _versionnedDocument;
        }

        public String value;
    }

    @DocumentDef(domain="test",name="test",version = "2.1.0")
    public static class TestModelV2 extends TestModel {

    }

    public static class UpgraderClass {
        @DocumentVersionUpgrader(domain = "test", name = "test", from = "1.0", to = "1.1.0")
        public TestModel upgradeToV1_1(TestModel src) {
            src.value += " v1.1";
            return src;
        }

        @DocumentVersionUpgrader(domain = "test", name = "test", from = "1.1", to = "2.0.0")
        public TestModelV2 upgradeToV2(TestModel src) {
            TestModelV2 v1_1 = new TestModelV2();
            v1_1.value = src.value + " v2";
            return v1_1;
        }
    }

    @Test
    public void upgradeUnitTests(){
        TestModel v1 = new TestModel();
        String refValue = "A first Value";
        v1.value = refValue;
        Object result = Utils.performUpgrade(v1, "test", "test", "1.0.0");
        assertEquals(result.getClass(),TestModelV2.class);
        assertEquals(refValue+" v1.1 v2",((TestModelV2)result).value);
        assertEquals("test/test/2.0.0",((TestModelV2)result).getDocumentFullVersionId());

    }

}

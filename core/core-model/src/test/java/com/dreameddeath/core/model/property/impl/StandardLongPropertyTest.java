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

package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.NumericProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StandardLongPropertyTest {
    @Test
    public void test() {
        CouchbaseDocument doc = new CouchbaseDocument() {};
        doc.getBaseMeta().setStateSync();

        NumericProperty<Long> std_prop = new StandardLongProperty(doc);
        NumericProperty<Long> std_prop_with_default = new StandardLongProperty(doc, 10L);

        //check default value and mark as dirty
        assertNull(std_prop.get());
        assertEquals(10L,(long) std_prop_with_default.get());
        assertEquals(CouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //Set Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.set(10L);
        assertEquals(10L,(long) std_prop.get());
        assertEquals(CouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //Inc Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.inc(15L);
        assertEquals(25L, (long)std_prop.get());
        assertEquals(CouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //Dec Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.dec(5L);
        assertEquals(20L,(long) std_prop.get());
        assertEquals(CouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //mul Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.mul(2);
        assertEquals(40L,(long) std_prop.get());
        assertEquals(CouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //div Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.div(2);
        assertEquals(20L,(long) std_prop.get());
        assertEquals(CouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());
    }
}
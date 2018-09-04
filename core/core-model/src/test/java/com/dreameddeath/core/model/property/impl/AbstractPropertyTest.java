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

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.v2.DocumentState;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractPropertyTest {
    public static class TestElement extends CouchbaseDocumentElement{
        /**
         *  test : test
         */
        @DocumentProperty("test")
        private Property<String> _test = new StandardProperty<String>(TestElement.this);

        // test accessors
        public String getTest() { return _test.get(); }
        public void setTest(String val) { _test.set(val); }
    }

    @Test
    public void testGetRawValue() throws Exception {
        CouchbaseDocument doc=new CouchbaseDocument(){};
        doc.getBaseMeta().setStateSync();
        AbstractProperty<String> test = new AbstractProperty<>(doc);
        AbstractProperty<String> test_with_default = new AbstractProperty<String>(doc,"test");
        AbstractProperty<TestElement> test_with_defaultClass= new AbstractProperty<>(doc,TestElement.class);
        //Assert
        assertNull(test.getRawValue());
        assertNull(test_with_default.getRawValue());
        assertNull(test_with_defaultClass.getRawValue());
        assertEquals(DocumentState.SYNC,doc.getBaseMeta().getState());
    }

    @Test
    public void testGet() throws Exception {
        CouchbaseDocument doc=new CouchbaseDocument(){};
        doc.getBaseMeta().setStateSync();
        AbstractProperty<String> test = new AbstractProperty<String>(doc);
        AbstractProperty<String> test_with_default = new AbstractProperty<String>(doc,"test");
        AbstractProperty<TestElement> test_with_defaultClass= new AbstractProperty<>(doc,TestElement.class);

        //Test get with default value
        assertNull(test.get());
        assertEquals(DocumentState.SYNC, doc.getBaseMeta().getState());
        assertEquals("test", test_with_default.get());
        assertEquals(DocumentState.DIRTY,doc.getBaseMeta().getState());
        doc.getBaseMeta().setStateSync();
        assertNotNull(test_with_defaultClass.get());
        assertEquals(DocumentState.DIRTY,doc.getBaseMeta().getState());
        doc.getBaseMeta().setStateSync();
        assertNotNull(test_with_defaultClass.get());
        assertEquals(DocumentState.SYNC, doc.getBaseMeta().getState());
        test_with_defaultClass.get().setTest("toto");
        assertEquals(DocumentState.DIRTY, doc.getBaseMeta().getState());
    }

    @Test
    public void testSet() throws Exception {
        CouchbaseDocument doc=new CouchbaseDocument(){};
        doc.getBaseMeta().setStateSync();
        AbstractProperty<String> test = new AbstractProperty<String>(doc);

        test.set("test");
        assertEquals("test",test.get());
        assertEquals(DocumentState.DIRTY,doc.getBaseMeta().getState());
    }

    @Test
    public void testEquals() throws Exception {
        CouchbaseDocument doc=new CouchbaseDocument(){};
        doc.getBaseMeta().setStateSync();
        AbstractProperty<String> test = new AbstractProperty<String>(doc);
        test.set("test");
        AbstractProperty<String> test_clone = new AbstractProperty<String>(doc);
        test_clone.set("test");
        AbstractProperty<String> test2 = new AbstractProperty<String>(doc);
        test2.set("test2");

        assertNotEquals(test,doc);
        assertEquals(test,test);
        assertEquals(test,test_clone);
        assertNotEquals(test,test2);
    }

    @Test
    public void testEqualsValue() throws Exception {

    }
}
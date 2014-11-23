package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractPropertyTest {

    @Test
    public void testGetRawValue() throws Exception {
        RawCouchbaseDocument doc=new RawCouchbaseDocument(){};
        doc.getBaseMeta().setStateSync();
        AbstractProperty<String> test = new AbstractProperty<String>(doc);
        AbstractProperty<String> test_with_default = new AbstractProperty<String>(doc,"test");

        //Assert
        assertNull(test.getRawValue());
        assertNull(test_with_default.getRawValue());
        assertEquals(RawCouchbaseDocument.DocumentState.SYNC,doc.getBaseMeta().getState());
    }

    @Test
    public void testGet() throws Exception {
        RawCouchbaseDocument doc=new RawCouchbaseDocument(){};
        doc.getBaseMeta().setStateSync();
        AbstractProperty<String> test = new AbstractProperty<String>(doc);
        AbstractProperty<String> test_with_default = new AbstractProperty<String>(doc,"test");

        //Test get with default value
        assertNull(test.get());
        assertEquals(RawCouchbaseDocument.DocumentState.SYNC, doc.getBaseMeta().getState());
        assertEquals("test", test_with_default.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());
    }

    @Test
    public void testSet() throws Exception {
        RawCouchbaseDocument doc=new RawCouchbaseDocument(){};
        doc.getBaseMeta().setStateSync();
        AbstractProperty<String> test = new AbstractProperty<String>(doc);

        test.set("test");
        assertEquals("test",test.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());
    }

    @Test
    public void testEquals() throws Exception {
        RawCouchbaseDocument doc=new RawCouchbaseDocument(){};
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
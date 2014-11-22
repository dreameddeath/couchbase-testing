package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class ArrayListPropertyTest {
    @Test
    public void testSubProperty() throws Exception{
        RawCouchbaseDocument doc=new RawCouchbaseDocument(){};

        ArrayListProperty<StandardLongProperty> test = new ArrayListProperty<StandardLongProperty>(doc);
        StandardLongProperty prop=new StandardLongProperty(doc,10L);
        test.add(prop);

        assertEquals(doc,prop.getParentElement());
        doc.getBaseMeta().setStateSync();

        assertEquals(10L,(long)prop.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());



    }

    @Test
    public void testAdd() throws Exception {
        RawCouchbaseDocument doc=new RawCouchbaseDocument(){};

        ArrayListProperty<String> test = new ArrayListProperty<String>(doc);
        //Assert
        doc.getBaseMeta().setStateSync();
        test.add("test");
        assertEquals(1, test.size());
        assertEquals("test",test.get(0));
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //Assert
        doc.getBaseMeta().setStateSync();
        test.add("test2");
        assertEquals(2,test.size());
        assertEquals("test2",test.get(1));
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());


        //TODO tests with "HasElement" object;
    }

    @Test
    public void testRemove() throws Exception {
        RawCouchbaseDocument doc=new RawCouchbaseDocument(){};
        ArrayListProperty<String> test = new ArrayListProperty<String>(doc);

        test.add("test");
        test.add("test2");
        test.add("test3");
        test.add("test4");
        //Assert
        doc.getBaseMeta().setStateSync();
        assertTrue(test.remove("test3"));
        assertArrayEquals(new String[]{"test","test2","test4"},test.toArray());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());
        //Assert
        doc.getBaseMeta().setStateSync();
        assertEquals("test2", test.remove(1));
        assertArrayEquals(new String[]{"test","test4"},test.toArray());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //TODO tests with "HasElement" object;
    }


    @Test
    public void testSet() throws Exception {
        RawCouchbaseDocument doc=new RawCouchbaseDocument(){};
        ArrayListProperty<String> test = new ArrayListProperty<String>(doc);

        test.add("test");
        test.add("test2");
        test.add("test3");
        test.add("test4");

        //Replace position 2
        doc.getBaseMeta().setStateSync();
        assertEquals("test3", test.set(2, "test3bis"));
        assertArrayEquals(new String[]{"test", "test2", "test3bis", "test4"}, test.toArray());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //Replace whole list
        doc.getBaseMeta().setStateSync();
        test.set((Collection<String>)Arrays.asList( new String[]{"test1bis","test2bis","test3bis"}));
        assertArrayEquals(new String[]{"test1bis","test2bis","test3bis"}, test.toArray());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //TODO tests with "HasElement" object;

    }

    @Test
    public void testClear() throws Exception {

    }



    @Test
    public void testAddAll() throws Exception {

    }

    @Test
    public void testGetProp() throws Exception {

    }

    @Test
    public void testSetProp() throws Exception {

    }

}
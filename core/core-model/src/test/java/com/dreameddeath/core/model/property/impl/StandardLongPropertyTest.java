package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import com.dreameddeath.core.model.property.NumericProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StandardLongPropertyTest {
    @Test
    public void test() {
        RawCouchbaseDocument doc = new RawCouchbaseDocument() {};
        doc.getBaseMeta().setStateSync();

        NumericProperty<Long> std_prop = new StandardLongProperty(doc);
        NumericProperty<Long> std_prop_with_default = new StandardLongProperty(doc, 10L);

        //check default value and mark as dirty
        assertNull(std_prop.get());
        assertEquals(10L,(long) std_prop_with_default.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //Set Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.set(10L);
        assertEquals(10L,(long) std_prop.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //Inc Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.inc(15L);
        assertEquals(25L, (long)std_prop.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //Dec Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.dec(5L);
        assertEquals(20L,(long) std_prop.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //mul Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.mul(2);
        assertEquals(40L,(long) std_prop.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());

        //div Value and check
        doc.getBaseMeta().setStateSync();
        std_prop.div(2);
        assertEquals(20L,(long) std_prop.get());
        assertEquals(RawCouchbaseDocument.DocumentState.DIRTY,doc.getBaseMeta().getState());
    }
}
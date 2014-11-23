package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import junit.framework.TestCase;

public class ImmutablePropertyTest extends TestCase {

    public void test(){
        //Init Doc
        RawCouchbaseDocument dummyDoc = new RawCouchbaseDocument(){};
        dummyDoc.getBaseMeta().setStateSync();

        //Init Property
        ImmutableProperty<String> testStr = new ImmutableProperty<String>(dummyDoc);

        //Check Set
        testStr.set("str");
        assertEquals(testStr.get(),"str");
        assertEquals(dummyDoc.getBaseMeta().getState(), RawCouchbaseDocument.DocumentState.DIRTY);

        //Check reset with same value
        testStr.set("str");

        //Check reset with different value
        try {
            testStr.set("otherValue");
            fail("Should have thrown exception");
        }
        catch(UnsupportedOperationException e){
            //Nominal Case
        }
        catch(Exception e) {
            fail("Should have throw UnsupportedOperationException");
        }
    }
}
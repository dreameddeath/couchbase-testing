package com.dreameddeath.common.model;

import com.dreameddeath.common.model.property.ImmutableProperty;
import junit.framework.TestCase;

public class ImmutablePropertyTest extends TestCase {

    public void test(){
        //Init Doc
        CouchbaseDocument dummyDoc = new CouchbaseDocument(){};
        dummyDoc.setStateSync();

        //Init Property
        ImmutableProperty<String> testStr = new ImmutableProperty<String>(dummyDoc);

        //Check Set
        testStr.set("str");
        assertEquals(testStr.get(),"str");
        assertEquals(dummyDoc.getState(),CouchbaseDocument.State.DIRTY);

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
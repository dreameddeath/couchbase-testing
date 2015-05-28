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

package com.dreameddeath.core.model;

import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import junit.framework.TestCase;

public class ImmutablePropertyTest extends TestCase {

    public void test(){
        //Init Doc
        CouchbaseDocument dummyDoc = new CouchbaseDocument(){};
        dummyDoc.getBaseMeta().setStateSync();

        //Init Property
        ImmutableProperty<String> testStr = new ImmutableProperty<String>(dummyDoc);

        //Check Set
        testStr.set("str");
        assertEquals(testStr.get(),"str");
        assertEquals(dummyDoc.getBaseMeta().getState(), CouchbaseDocument.DocumentState.DIRTY);

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
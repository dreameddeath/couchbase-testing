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

package com.dreameddeath.core.service.swagger;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 27/02/2015.
 */
public class TestingDocument extends CouchbaseDocument {
    /**
     *  testList : simple List
     */
    @DocumentProperty("testList")
    private ListProperty<String> testList = new ArrayListProperty<String>(TestingDocument.this);
    /**
     *  testCplxList : to test complex List
     */
    @DocumentProperty("testCplxList")
    private ListProperty<TestingInnerElement> testCplxList = new ArrayListProperty<TestingInnerElement>(TestingDocument.this);
    /**
     *  cplxElement : Complex Element Test
     */
    @DocumentProperty("cplxElement")
    private Property<TestingInnerElement> cplxElement = new StandardProperty<TestingInnerElement>(TestingDocument.this);
    /**
     *  dateTest : date test
     */
    @DocumentProperty("dateTest")
    private Property<DateTime> dateTest = new StandardProperty<DateTime>(TestingDocument.this);
    /**
     *  dateTestStd : date test
     */
    @DocumentProperty("dateTestStd")
    private DateTime dateTestStd;
    /**
     *  testExternalEltList : List of external Elements
     */
    @DocumentProperty("testExternalEltList")
    private ListProperty<TestingExternalElement> testExternalEltList = new ArrayListProperty<TestingExternalElement>(TestingDocument.this);


    // TestList Accessors
    public List<String> getTestList() { return testList.get(); }
    public void setTestList(Collection<String> vals) { testList.set(vals); }
    public boolean addTestList(String val){ return testList.add(val); }
    public boolean removeTestList(String val){ return testList.remove(val); }
    // TestCplxList Accessors
    public List<TestingInnerElement> getTestCplxList() { return testCplxList.get(); }
    public void setTestCplxList(Collection<TestingInnerElement> vals) { testCplxList.set(vals); }
    public boolean addTestCplxList(TestingInnerElement val){ return testCplxList.add(val); }
    public boolean removeTestCplxList(TestingInnerElement val){ return testCplxList.remove(val); }
    // cplxElement accessors
    public TestingInnerElement getCplxElement() { return cplxElement.get(); }
    public void setCplxElement(TestingInnerElement val) { cplxElement.set(val); }
    // dateTest accessors
    public DateTime getDateTest() { return dateTest.get(); }
    public void setDateTest(DateTime val) { dateTest.set(val); }
    // dateTestStd accessors
    public DateTime getDateTestStd() {return dateTestStd;}
    public void setDateTestStd(DateTime dateTestStd) {this.dateTestStd = dateTestStd;}
    // TestExternalEltList Accessors
    public List<TestingExternalElement> getTestExternalEltList() { return testExternalEltList.get(); }
    public void setTestExternalEltList(Collection<TestingExternalElement> vals) { testExternalEltList.set(vals); }
    public boolean addTestExternalEltList(TestingExternalElement val){ return testExternalEltList.add(val); }
    public boolean removeTestExternalEltList(TestingExternalElement val){ return testExternalEltList.remove(val); }





    public static class TestingInnerElement extends CouchbaseDocumentElement {
        /**
         *  date : DateTime test
         */
        @DocumentProperty("date")
        private ListProperty<DateTime> date = new ArrayListProperty<DateTime>(TestingInnerElement.this);

        // Date Accessors
        public List<DateTime> getDate() { return date.get(); }
        public void setDate(Collection<DateTime> vals) { date.set(vals); }
        public boolean addDate(DateTime val){ return date.add(val); }
        public boolean removeDate(DateTime val){ return date.remove(val); }

    }
}

/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *  
 */

package com.tests;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.MapProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.HashMapProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.query.annotation.QueryExpose;
import com.dreameddeath.core.query.annotation.QueryFieldInfo;
import com.dreameddeath.core.query.annotation.QueryFieldFilteringMode;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
@DocumentEntity(domain="test",version = "1.0")
@QueryExpose(rootPath = "test",domain = "tests")
public class TestGeneration extends CouchbaseDocument {
    @DocumentProperty
    public Integer decrIntValue;
    @DocumentProperty
    private String docKey;
    @DocumentProperty @QueryFieldInfo
    public Integer resultIncrValue;
    @DocumentProperty @QueryFieldInfo
    private String outputString;
    @DocumentProperty("simpleWrappedSimple") 
    private Property<String> simpleWrappedSimple = new StandardProperty<>(TestGeneration.this);
    @DocumentProperty("simpleList") 
    private ListProperty<DateTime> simpleList = new ArrayListProperty<>(TestGeneration.this);
    @DocumentProperty("complexList") 
    private ListProperty<TestSubGenerator> complexList = new ArrayListProperty<>(TestGeneration.this);
    @DocumentProperty("enumType")  @QueryFieldInfo
    private Property<NewEnumType> enumType = new StandardProperty<>(TestGeneration.this);
    @DocumentProperty("listEnum")  @QueryFieldInfo
    private ListProperty<NewEnumType> listEnum = new ArrayListProperty<>(TestGeneration.this);
    @DocumentProperty("unwrapped")  @QueryFieldInfo(unwrap = true,mode=QueryFieldFilteringMode.STANDARD)
    private Property<UnwrappedClass> unwrapped = new StandardProperty<>(TestGeneration.this);
    @DocumentProperty("inheritedList") @QueryFieldInfo(mode = QueryFieldFilteringMode.FULL) 
    private ListProperty<InheritedClass> inheritedList = new ArrayListProperty<>(TestGeneration.this);

    public String getDocKey(){
        return docKey;
    }
    public void setDocKey(String key){
        this.docKey=key;
    }

    public String getOutputString() {
        return outputString;
    }
    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public String getSimpleWrappedSimple() { return simpleWrappedSimple.get(); }
    public void setSimpleWrappedSimple(String val) { simpleWrappedSimple.set(val); }


    public List<DateTime> getSimpleList() { return simpleList.get(); }
    public void setSimpleList(Collection<DateTime> newSimpleList) { simpleList.set(newSimpleList); }


    public List<TestSubGenerator> getComplexList() { return complexList.get(); }
    public void setComplexList(Collection<TestSubGenerator> newComplexList) { complexList.set(newComplexList); }

    public NewEnumType getEnumType() { return enumType.get(); }
    public void setEnumType(NewEnumType val) { enumType.set(val); }

    public List<NewEnumType> getListEnum() { return listEnum.get(); }
    public void setListEnum(Collection<NewEnumType> newListEnum) { listEnum.set(newListEnum); }

    public UnwrappedClass getUnwrapped() { return unwrapped.get(); }
    public void setUnwrapped(UnwrappedClass val) { unwrapped.set(val); }

    public List<InheritedClass> getInheritedList() { return inheritedList.get(); }
    public void setInheritedList(Collection<InheritedClass> newInheritedList) { inheritedList.set(newInheritedList); }


    public static class TestSubGenerator extends CouchbaseDocumentElement{
        @DocumentProperty("recursiveMap")
        private MapProperty<String,TestSubGenerator> recursiveMap = new HashMapProperty<>(TestSubGenerator.this);

        public Map<String,TestSubGenerator> getRecursiveMap() { return recursiveMap.get(); }
        public void setRecursiveMap(Map<String,TestSubGenerator> newRecursiveMap) { recursiveMap.set(newRecursiveMap); }
    }

    public enum NewEnumType{
        TOTO,
        TUTU,
        TITI;
    }

    public static class UnwrappedClass extends CouchbaseDocumentElement{
        @DocumentProperty("string wrapped test") @QueryFieldInfo
        private Property<String> stringWrappedTest = new StandardProperty<>(UnwrappedClass.this);
        @DocumentProperty("recursiveUnwrapped")  @QueryFieldInfo
        private Property<RecursiveUnwrapped> recursiveUnwrapped = new StandardProperty<>(UnwrappedClass.this);
        @DocumentProperty("recursiveUnwrappedRead") @QueryFieldInfo(unwrap = true)
        private Property<RecursiveUnwrapped> recursiveUnwrappedRead = new StandardProperty<>(UnwrappedClass.this);

        public String getStringWrappedTest() { return stringWrappedTest.get(); }
        public void setStringWrappedTest(String val) { stringWrappedTest.set(val); }

        public RecursiveUnwrapped getRecursiveUnwrapped() { return recursiveUnwrapped.get(); }
        public void setRecursiveUnwrapped(RecursiveUnwrapped val) { recursiveUnwrapped.set(val); }

        public RecursiveUnwrapped getRecursiveUnwrappedRead() { return recursiveUnwrappedRead.get(); }
        public void setRecursiveUnwrappedRead(RecursiveUnwrapped val) { recursiveUnwrappedRead.set(val); }
    }

    public static class RecursiveUnwrapped extends CouchbaseDocumentElement{
        @DocumentProperty("subListString")  @QueryFieldInfo
        private ListProperty<String> subListString = new ArrayListProperty<>(RecursiveUnwrapped.this);

        public List<String> getSubListString() { return subListString.get(); }
        public void setSubListString(Collection<String> newSubListString) { subListString.set(newSubListString); }
    }
    

    public abstract static class InheritedClass extends CouchbaseDocumentElement{
        /**
         *  test : sublevel 1
         */
        @DocumentProperty("testLvl1")
        private Property<String> testLvl1 = new StandardProperty<>(InheritedClass.this);
        
        /**
         * Getter of test 
         * @return the value of test
         */
        public String getTestLvl1() { return testLvl1.get(); }
        /**
         * Setter of test 
         * @param val the new value of test
         */
        public void setTestLvl1(String val) { testLvl1.set(val); }
    }

    @DocumentEntity
    public static class InheritedSubLvl1Class extends InheritedClass{
        /**
         *  testLvl2 : sublevel field
         */
        @DocumentProperty("testLvl2")
        private Property<String> testLvl2 = new StandardProperty<>(InheritedSubLvl1Class.this);

        /**
         * Getter of testLvl2
         * @return the value of testLvl2
         */
        public String getTestLvl2() { return testLvl2.get(); }
        /**
         * Setter of testLvl2
         * @param val the new value of testLvl2
         */
        public void setTestLvl2(String val) { testLvl2.set(val); }
    }

    @DocumentEntity
    public abstract static class InheritedSubLvl1BisClass extends InheritedClass{
        /**
         *  testLevel2Bis : sub level field
         */
        @DocumentProperty("testLevel2Bis")
        private Property<String> testLevel2Bis = new StandardProperty<>(InheritedSubLvl1BisClass.this);

        /**
         * Getter of testLevel2Bis
         * @return the value of testLevel2Bis
         */
        public String getTestLevel2Bis() { return testLevel2Bis.get(); }
        /**
         * Setter of testLevel2Bis
         * @param val the new value of testLevel2Bis
         */
        public void setTestLevel2Bis(String val) { testLevel2Bis.set(val); }
    }


    @DocumentEntity
    public static class InheritedSubLvl2Class extends InheritedSubLvl1BisClass{
        /**
         *  testLvl3 : Level 3 testing
         */
        @DocumentProperty("testLvl3")
        private Property<String> testLvl3 = new StandardProperty<>(InheritedSubLvl2Class.this);

        /**
         * Getter of testLvl3
         * @return the value of testLvl3
         */
        public String getTestLvl3() { return testLvl3.get(); }
        /**
         * Setter of testLvl3
         * @param val the new value of testLvl3
         */
        public void setTestLvl3(String val) { testLvl3.set(val); }
    }

}

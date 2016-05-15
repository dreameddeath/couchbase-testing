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

package com.tests;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.MapProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.HashMapProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.couchbase.core.process.remote.annotation.FieldFilteringMode;
import com.dreameddeath.couchbase.core.process.remote.annotation.Request;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;
import com.dreameddeath.couchbase.core.process.remote.annotation.Result;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
@DocumentEntity(domain="test",version = "1.0")
@RestExpose(rootPath = "testdocjobs/genupdate",domain = "tests",name = "testdocjobupdategen")
public class TestGeneration extends AbstractJob {
    @DocumentProperty @NotNull @Request
    public Integer decrIntValue;
    @DocumentProperty @NotNull @Request
    private String docKey;
    @DocumentProperty @Result
    public Integer resultIncrValue;
    @DocumentProperty @Result
    private String outputString;
    @DocumentProperty("simpleWrappedSimple") @Request
    private Property<String> simpleWrappedSimple = new StandardProperty<>(TestGeneration.this);
    @DocumentProperty("simpleList") @Request
    private ListProperty<DateTime> simpleList = new ArrayListProperty<>(TestGeneration.this);
    @DocumentProperty("complexList") @Request(mode=FieldFilteringMode.FULL)
    private ListProperty<TestSubGenerator> complexList = new ArrayListProperty<>(TestGeneration.this);
    @DocumentProperty("enumType") @Request @Result
    private Property<NewEnumType> enumType = new StandardProperty<>(TestGeneration.this);
    @DocumentProperty("listEnum") @Request @Result
    private ListProperty<NewEnumType> listEnum = new ArrayListProperty<>(TestGeneration.this);
    @DocumentProperty("unwrapped") @Request(unwrap = true,mode = FieldFilteringMode.FULL) @Result(unwrap = true,mode=FieldFilteringMode.STANDARD)
    private Property<UnwrappedClass> unwrapped = new StandardProperty<>(TestGeneration.this);

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
        @DocumentProperty("string wrapped test") @Result
        private Property<String> stringWrappedTest = new StandardProperty<>(UnwrappedClass.this);
        @DocumentProperty("recursiveUnwrapped") @Request(unwrap = true) @Result
        private Property<RecursiveUnwrapped> recursiveUnwrapped = new StandardProperty<>(UnwrappedClass.this);
        @DocumentProperty("recursiveUnwrappedRead") @Result(unwrap = true)
        private Property<RecursiveUnwrapped> recursiveUnwrappedRead = new StandardProperty<>(UnwrappedClass.this);

        public String getStringWrappedTest() { return stringWrappedTest.get(); }
        public void setStringWrappedTest(String val) { stringWrappedTest.set(val); }

        public RecursiveUnwrapped getRecursiveUnwrapped() { return recursiveUnwrapped.get(); }
        public void setRecursiveUnwrapped(RecursiveUnwrapped val) { recursiveUnwrapped.set(val); }

        public RecursiveUnwrapped getRecursiveUnwrappedRead() { return recursiveUnwrappedRead.get(); }
        public void setRecursiveUnwrappedRead(RecursiveUnwrapped val) { recursiveUnwrappedRead.set(val); }
    }

    public static class RecursiveUnwrapped extends CouchbaseDocumentElement{
        @DocumentProperty("subListString") @Request @Result
        private ListProperty<String> subListString = new ArrayListProperty<>(RecursiveUnwrapped.this);

        public List<String> getSubListString() { return subListString.get(); }
        public void setSubListString(Collection<String> newSubListString) { subListString.set(newSubListString); }
    }

}

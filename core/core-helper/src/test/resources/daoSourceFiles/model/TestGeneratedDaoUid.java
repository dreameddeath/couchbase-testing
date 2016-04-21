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

package model;

import java.util.Collection;
import java.util.List;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;

import com.dreameddeath.core.helper.annotation.dao.Counter;
import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.helper.annotation.dao.UidDef;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.business.model.BusinessDocument;

import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

@DocumentEntity(domain="test",name="daoProccessorUid",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDaoWithUID.class,dbPath = "testUid/",idFormat = "%010d",idPattern = "\\d{10}")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
@Counter(name= "checks",dbName = "checks")
@UidDef(fieldName = "uid")
public class TestGeneratedDaoUid extends BusinessDocument {
    @DocumentProperty("uid")
    public String uid;
    @DocumentProperty("value")
    public String value;
    /**
     *  testList : List of strings
     */
    @DocumentProperty("testList")
    private ListProperty<String> testList = new ArrayListProperty<String>(TestGeneratedDaoUid.this);

    // TestList Accessors
    public List<String> getTestList() { return testList.get(); }
    public void setTestList(Collection<String> vals) { testList.set(vals); }
    public boolean addTestList(String val){ return testList.add(val); }
    public boolean removeTestList(String val){ return testList.remove(val); }
}
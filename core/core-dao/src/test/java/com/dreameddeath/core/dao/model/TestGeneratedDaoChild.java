/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.dao.model;

import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.annotation.dao.ParentEntity;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import model.ITestDao;
import model.ITestDaoChild;


@DocumentEntity(domain="testDaoGeneration",name="daoProccessorChild",version = "1.0.0")
@DaoEntity(baseDao = CouchbaseDocumentWithKeyPatternDao.class,
        dbPath = "child/",
        idFormat = "%010d",
        idPattern = "\\d{10}")
@ParentEntity(c= TestGeneratedDao.class,keyPath = "parent.key",separator = "/")
    @Counter(name="cnt",dbName = "cnt",isKeyGen = true)
public class TestGeneratedDaoChild extends CouchbaseDocument implements ITestDaoChild {
    @DocumentProperty("value")
    public String value;
    @DocumentProperty("parent")
    public TestGeneratedDaoLink parent;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    public TestGeneratedDaoLink getParent() {
        return parent;
    }

    public void setParent(TestGeneratedDaoLink parent) {
        this.parent = parent;
    }

    @Override
    public void setParentObjDao(ITestDao parent){
        this.parent = new TestGeneratedDaoLink((TestGeneratedDao)parent);
    }
}
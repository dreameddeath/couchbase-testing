/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package model;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.dao.helper.annotation.DaoEntity;
import com.dreameddeath.core.dao.helper.annotation.Counter;
import com.dreameddeath.core.dao.helper.annotation.ParentEntity;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;

@DocumentDef(domain="test",name="daoProccessorChild",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDao.class,dbPath = "child/",idFormat = "%010d",idPattern = "\\d{10}")
@ParentEntity(c=TestDao.class,keyPath = "parent.key",separator = "/")
@Counter(name="cnt",dbName = "cnt",isKeyGen = true)
public class TestDaoChild extends BusinessCouchbaseDocument {
    @DocumentProperty("value")
    public String value;
    @DocumentProperty("parent")
    public TestDaoLink parent;
}
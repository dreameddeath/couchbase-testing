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

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.helper.annotation.dao.*;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.dao.model.view.impl.ViewStringKeyTranscoder;
import com.dreameddeath.core.dao.model.view.impl.ViewStringTranscoder;

@DocumentDef(domain="test",name="daoProccessor",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDao.class,dbPath = "test/",idFormat = "%010d",idPattern = "\\d{10}",rest = true)
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
@Counter(name= "checks",dbName = "checks")
@View(name = "listView",
        contentFilename = "viewContent.cbView",
        keyDef = @ViewKeyDef(type=String.class,transcoder = ViewStringKeyTranscoder.class),
        valueDef = @ViewValueDef(type=String.class,transcoder = ViewStringTranscoder.class))
@View(name = "valueView",
        content = "emit(doc.value,doc.value);",
        keyDef = @ViewKeyDef(type=String.class,transcoder = ViewStringKeyTranscoder.class),
        valueDef = @ViewValueDef(type=String.class,transcoder = ViewStringTranscoder.class))
public class TestDao extends BusinessDocument {
    @DocumentProperty("value")
    public String value;
}
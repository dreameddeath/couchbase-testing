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

package com.dreameddeath.core.query.model;

import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.query.annotation.QueryExpose;

/**
 * Created by CEAJ8230 on 18/06/2017.
 */
@DocumentEntity(domain = "test",version = "1.0.0")
@QueryExpose(domain = "test",rootPath = "/queries")
@DaoEntity(baseDao = CouchbaseDocumentWithKeyPatternDao.class,dbPath = "test",idFormat = "%05d",idPattern = "\\d{10}")
@Counter(name="cnt",dbName = "cnt",isKeyGen = true)
public class TestModel extends CouchbaseDocument {
    @DocumentProperty("strValue")
    public String strValue;
}

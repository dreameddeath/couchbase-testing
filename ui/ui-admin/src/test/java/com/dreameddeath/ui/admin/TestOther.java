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

package com.dreameddeath.ui.admin;

import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.query.annotation.QueryExpose;

/**
 * Created by Christophe Jeunesse on 09/11/2015.
 */
@QueryExpose(domain = "test",rootPath = "/queries/testothers",superClassGenMode = SuperClassGenMode.UNWRAP)
@DocumentEntity(domain = "test",name="other")
@DaoEntity(baseDao = BusinessCouchbaseDocumentWithKeyPatternDao.class,dbPath = "/other",idFormat = "/%d",idPattern = "/\\d+")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
public class TestOther extends BusinessDocument {
}

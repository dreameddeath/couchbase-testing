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

package com.dreameddeath.ui.admin.process;

import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.helper.annotation.dao.Counter;
import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
@DocumentEntity(domain = "test")
@DaoEntity(baseDao = BusinessCouchbaseDocumentWithKeyPatternDao.class,dbPath = "test/",idFormat = "%010d",idPattern = "\\d{10}",rest = true)
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
public class TestDocProcess extends BusinessDocument {
    @DocumentProperty("name")
    public String name;
}

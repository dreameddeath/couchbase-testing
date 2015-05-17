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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
public abstract class BaseCouchbaseDocumentWithKeyPatternDao<T extends BaseCouchbaseDocument> extends BaseCouchbaseDocumentDao<T> {
    public abstract String getKeyPattern();

    public BaseCouchbaseDocumentWithKeyPatternDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

}

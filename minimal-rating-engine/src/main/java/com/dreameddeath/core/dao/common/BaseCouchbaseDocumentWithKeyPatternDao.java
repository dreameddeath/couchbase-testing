package com.dreameddeath.core.dao.common;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
public abstract class BaseCouchbaseDocumentWithKeyPatternDao<T extends BaseCouchbaseDocument> extends BaseCouchbaseDocumentDao<T> {
    public abstract String getKeyPattern();

    public BaseCouchbaseDocumentWithKeyPatternDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

}

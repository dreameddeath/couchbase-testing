package com.dreameddeath.core.dao.common;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
public abstract class BaseCouchbaseDocumentWithKeyPatternDao<T extends RawCouchbaseDocument> extends BaseCouchbaseDocumentDao<T> {
    public abstract String getKeyPattern();
}

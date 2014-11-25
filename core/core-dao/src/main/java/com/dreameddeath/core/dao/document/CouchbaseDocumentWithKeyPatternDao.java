package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
public abstract class CouchbaseDocumentWithKeyPatternDao<T extends CouchbaseDocument> extends CouchbaseDocumentDao<T> {
    public abstract String getKeyPattern();
}

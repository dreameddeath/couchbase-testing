package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.model.business.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class CouchbaseDocumentValidator<T extends CouchbaseDocument> extends CouchbaseDocumentElementValidator<T> {
    public CouchbaseDocumentValidator(Class<T> rootObj, ValidatorFactory factory) {
        super(rootObj, factory);
    }
}
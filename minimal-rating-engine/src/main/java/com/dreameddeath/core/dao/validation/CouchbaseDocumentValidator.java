package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class CouchbaseDocumentValidator<T extends CouchbaseDocument> extends CouchbaseDocumentElementValidator<T> {
    public CouchbaseDocumentValidator(Class<T> rootObj, Map<Class<? extends CouchbaseDocumentElement>, Validator<? extends CouchbaseDocumentElement>> cache) {
        super(rootObj, cache);
    }
}
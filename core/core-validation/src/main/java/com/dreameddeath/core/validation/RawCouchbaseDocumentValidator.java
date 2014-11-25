package com.dreameddeath.core.validation;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public class RawCouchbaseDocumentValidator<T extends CouchbaseDocument> extends GenericDocumentItemValidator<T>  {
    public RawCouchbaseDocumentValidator(Class<T> rootObj,ValidatorFactory factory){
        super(rootObj,factory);
    }
}

package com.dreameddeath.core.validation;

import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class CouchbaseDocumentElementValidator<T extends CouchbaseDocumentElement> extends GenericDocumentItemValidator<T>{

    public CouchbaseDocumentElementValidator(Class<T> rootObj,ValidatorFactory factory){
        super(rootObj,factory);
    }

}

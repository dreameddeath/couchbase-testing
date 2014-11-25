package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

/**
 * Created by Christophe Jeunesse on 09/05/2014.
 */
public class StandardProperty<T> extends AbstractProperty<T> {

    public StandardProperty(BaseCouchbaseDocumentElement parent){
        super(parent);
    }

    public StandardProperty(BaseCouchbaseDocumentElement parent, T defaultValue){
        super(parent,defaultValue);
    }
}

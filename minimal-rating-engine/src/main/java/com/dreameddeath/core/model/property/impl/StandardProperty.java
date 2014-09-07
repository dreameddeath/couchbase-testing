package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.impl.AbstractProperty;

/**
 * Created by Christophe Jeunesse on 09/05/2014.
 */
public class StandardProperty<T> extends AbstractProperty<T> {

    public StandardProperty(CouchbaseDocumentElement parent){
        super(parent);
    }

    public StandardProperty(CouchbaseDocumentElement parent, T defaultValue){
        super(parent,defaultValue);
    }
}

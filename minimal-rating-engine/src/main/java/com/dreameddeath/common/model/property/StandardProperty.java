package com.dreameddeath.common.model.property;

import com.dreameddeath.common.model.CouchbaseDocumentElement;

/**
 * Created by ceaj8230 on 09/05/2014.
 */
public class StandardProperty<T> extends AbstractProperty<T>{

    public StandardProperty(CouchbaseDocumentElement parent){
        super(parent);
    }

    public StandardProperty(CouchbaseDocumentElement parent, T defaultValue){
        super(parent,defaultValue);
    }
}

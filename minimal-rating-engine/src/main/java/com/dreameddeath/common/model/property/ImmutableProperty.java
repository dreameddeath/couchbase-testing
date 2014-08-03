package com.dreameddeath.common.model.property;

import com.dreameddeath.common.model.document.CouchbaseDocumentElement;

public class ImmutableProperty<T> extends AbstractProperty<T> {

    public ImmutableProperty(CouchbaseDocumentElement parentElement){
        super(parentElement);
    }
    public ImmutableProperty(CouchbaseDocumentElement parentElement, T defaultValue){
        super(parentElement,defaultValue);
    }


    @Override
    public final boolean set(T value) {
        if(!equalsValue(value) && (_value!=null)){
            throw new UnsupportedOperationException("Cannot reassign value <"+_value+"> with newValue <"+_value+">");
        }
        return super.set(value);
    }
}

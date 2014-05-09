package com.dreameddeath.common.model;

public class ImmutableProperty<T> extends AbstractProperty<T> {

    public ImmutableProperty(CouchbaseDocumentElement parentElement){
        super(parentElement);
    }

    @Override
    public final boolean set(T value) {
        if((super.get()!=null) && !equalsValue(value)){
            throw new UnsupportedOperationException("Cannot reassign value <"+_value+"> with newValue <"+_value+">");
        }
        return super.set(value);
    }
}

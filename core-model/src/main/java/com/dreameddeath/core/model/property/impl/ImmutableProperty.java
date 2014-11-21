package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.property.HasParent;

public class ImmutableProperty<T> extends AbstractProperty<T> {

    public ImmutableProperty(HasParent parentElement){
        super(parentElement);
    }
    public ImmutableProperty(HasParent parentElement, T defaultValue){
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

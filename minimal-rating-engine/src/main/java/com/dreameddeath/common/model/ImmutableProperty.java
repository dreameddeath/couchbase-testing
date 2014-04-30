package com.dreameddeath.common.model;

import java.lang.UnsupportedOperationException;
import com.fasterxml.jackson.annotation.JsonValue;

public class ImmutableProperty<T> implements Property<T> {
    CouchbaseDocumentElement _parentElt;
    T _value;
    
    
    public ImmutableProperty(CouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }
    
    @JsonValue
    public final T get(){ return _value; }
    public final boolean set(T value) {
        if((_value!=null) && !equals(value)){
            throw new UnsupportedOperationException("Cannot reassign value <"+_value+"> with newValue <"+_value+">");
        }
        if(value!=null){
            _value = value;
            if((_parentElt!=null)&& (value instanceof CouchbaseDocumentElement)){
                ((CouchbaseDocumentElement)value).setParentElement(_parentElt);
            }
            return true;
        }
        else{
            return false;
        }
    }
    @Override
    public boolean equals(Object prop){
        if(prop == null){
            return false;
        }
        else if(prop == this){
            return true;
        }
        else if(prop instanceof ImmutableProperty){
            return equalsValue(((ImmutableProperty<T>)prop)._value);
        }
        else{
            return false;
        }
    }
    
    public boolean equalsValue(T value){
        if(_value == value){
            return true;
        }
        else if(_value !=null){
            return _value.equals(value);
        }
        else{
            return false;
        }
    }
    
    public int hashCode(){
        if(_value!=null){
            return _value.hashCode();
        }
        else{
            return 0;
        }
    }
    
    public String toString(){
        if(_value!=null){
            return _value.toString();
        }
        else{
            return "[null]";
        }
    }
}

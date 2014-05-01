package com.dreameddeath.common.model;

import java.lang.UnsupportedOperationException;
import com.fasterxml.jackson.annotation.JsonValue;

public abstract class SynchronizedLinkProperty<T,TDOC extends CouchbaseDocument> implements Property<T>  {
    CouchbaseDocumentLink<TDOC> _parentLink;
    T _cachedValue;
    
    public SynchronizedLinkProperty(CouchbaseDocumentLink<TDOC> parentLink){
        _parentLink=parentLink;
    }
    
    protected abstract T getRealValue(TDOC doc);
    
    public final T get(){ 
        if(_parentLink.getLinkedObject(true)!=null){
            _cachedValue = getRealValue(_parentLink.getLinkedObject());
        }
        
        return _cachedValue; 
    }
    
    public final boolean set(T value) {
        if(_cachedValue!=null){
            throw new UnsupportedOperationException("Cannot reassign value <"+_cachedValue+"> with newValue <"+_cachedValue+">");
        }
        if(value!=null){
            _cachedValue = value;
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
        else if(prop instanceof SynchronizedLinkProperty){
            return equalsValue(((SynchronizedLinkProperty<T,TDOC>)prop)._cachedValue);
        }
        else{
            return false;
        }
    }
    
    public boolean equalsValue(T value){
        if(_cachedValue == value){
            return true;
        }
        else if(_cachedValue !=null){
            return _cachedValue.equals(value);
        }
        else{
            return false;
        }
    }
    
    public int hashCode(){
        if(_cachedValue!=null){
            return _cachedValue.hashCode();
        }
        else{
            return 0;
        }
    }
    
    public String toString(){
        if(_cachedValue!=null){
            return _cachedValue.toString();
        }
        else{
            return "[null]";
        }
    }
}

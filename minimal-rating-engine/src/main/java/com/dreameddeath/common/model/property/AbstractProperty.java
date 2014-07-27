package com.dreameddeath.common.model.property;

import com.dreameddeath.common.model.document.CouchbaseDocumentElement;

/**
 * Created by ceaj8230 on 09/05/2014.
 */
public class AbstractProperty<T> implements Property<T> {
    CouchbaseDocumentElement _parentElt;
    T _value;


    public AbstractProperty(CouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }

    public AbstractProperty(CouchbaseDocumentElement parentElement,T defaultValue){
        _parentElt=parentElement;
        _value=defaultValue;
    }

    public T get(){ return _value; }
    public boolean set(T value) {
        if(!equalsValue(value)){
            _value = value;
            if(_parentElt!=null) {
                if (value instanceof CouchbaseDocumentElement) {
                    ((CouchbaseDocumentElement) value).setParentElement(_parentElt);
                }
                _parentElt.dirtyDocument();
            }
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean equals(Object ref){
        if(ref == null){
            return false;
        }
        else if(ref == this){
            return true;
        }
        else if(ref instanceof AbstractProperty){
            return equalsValue(((AbstractProperty)ref)._value);
        }
        else{
            return false;
        }
    }

    public boolean equalsValue(Object value){
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

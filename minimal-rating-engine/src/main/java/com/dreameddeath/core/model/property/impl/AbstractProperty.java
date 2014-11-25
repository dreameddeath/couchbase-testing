package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.Property;

/**
 * Created by Christophe Jeunesse on 09/05/2014.
 */
public class AbstractProperty<T> implements Property<T>,HasParentDocumentElement {
    BaseCouchbaseDocumentElement _parentElt;
    protected T _value;
    protected T _defaultValue;

    public AbstractProperty(BaseCouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }

    public AbstractProperty(BaseCouchbaseDocumentElement parentElement,T defaultValue){
        _parentElt=parentElement;
        _defaultValue=defaultValue;
        if((_defaultValue!=null) && (_defaultValue instanceof HasParentDocumentElement)){
            ((HasParentDocumentElement) _defaultValue).setParentDocumentElement(_parentElt);
        }
    }

    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentElement){ _parentElt=parentElement;}
    public BaseCouchbaseDocumentElement getParentDocumentElement(){return _parentElt;}

    protected T getRawValue(){return _value;}

    public T get(){ if(_value==null){_value =_defaultValue; _defaultValue=null;} return _value; }
    public boolean set(T value) {
        if(!equalsValue(value)){
            _value = value;
            if(_parentElt!=null) {
                if (value instanceof HasParentDocumentElement) {
                    ((HasParentDocumentElement) value).setParentDocumentElement(_parentElt);
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

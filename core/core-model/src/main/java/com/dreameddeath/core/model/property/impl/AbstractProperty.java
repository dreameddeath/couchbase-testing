/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.model.property.Property;

/**
 * Created by Christophe Jeunesse on 09/05/2014.
 */
public class AbstractProperty<T> implements Property<T>,HasParent {
    HasParent _parentElt;
    protected T _value;
    protected T _defaultValue;

    public AbstractProperty(HasParent parentElement){
        _parentElt=parentElement;
    }

    public AbstractProperty(HasParent parentElement,T defaultValue){
        _parentElt=parentElement;
        _defaultValue=defaultValue;
    }

    public void setParentElement(HasParent parentElement){ _parentElt=parentElement;}
    public HasParent getParentElement(){return _parentElt;}

    protected T getRawValue(){return _value;}

    public T get(){ if(_value==null){set(_defaultValue);} return _value; }
    public boolean set(T value) {
        if(!equalsValue(value)){
            _value = value;
            if(_parentElt!=null) {
                if (value instanceof HasParent) {
                    ((HasParent) value).setParentElement(_parentElt);
                }
                HasParent.Helper.dirtyParentDocument(this);
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

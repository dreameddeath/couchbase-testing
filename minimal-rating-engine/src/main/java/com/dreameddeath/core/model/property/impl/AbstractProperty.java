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

import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.Property;

/**
 * Created by Christophe Jeunesse on 09/05/2014.
 */
public class AbstractProperty<T> implements Property<T>,HasParentDocumentElement {
    BaseCouchbaseDocumentElement parentElt;
    protected T value;
    protected T defaultValue;

    public AbstractProperty(BaseCouchbaseDocumentElement parentElement){
        parentElt=parentElement;
    }

    public AbstractProperty(BaseCouchbaseDocumentElement parentElement,T defaultValue){
        parentElt=parentElement;
        this.defaultValue=defaultValue;
        if((defaultValue!=null) && (defaultValue instanceof HasParentDocumentElement)){
            ((HasParentDocumentElement) defaultValue).setParentDocumentElement(parentElt);
        }
    }

    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentElement){ parentElt=parentElement;}
    public BaseCouchbaseDocumentElement getParentDocumentElement(){return parentElt;}

    protected T getRawValue(){return value;}

    public T get(){ if(value==null){value =defaultValue; defaultValue=null;} return value; }
    public boolean set(T value) {
        if(!equalsValue(value)){
            this.value = value;
            if(parentElt!=null) {
                if (value instanceof HasParentDocumentElement) {
                    ((HasParentDocumentElement) value).setParentDocumentElement(parentElt);
                }
                parentElt.dirtyDocument();
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
            return equalsValue(((AbstractProperty)ref).value);
        }
        else{
            return false;
        }
    }

    public boolean equalsValue(Object value){
        if(value == value){
            return true;
        }
        else if(value !=null){
            return value.equals(value);
        }
        else{
            return false;
        }
    }

    public int hashCode(){
        if(value!=null){
            return value.hashCode();
        }
        else{
            return 0;
        }
    }

    public String toString(){
        if(value!=null){
            return value.toString();
        }
        else{
            return "[null]";
        }
    }
}

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
    HasParent parentElt;
    protected T value;
    protected final boolean hasDefaultValue;
    protected final T defaultValue;
    protected final Class<T> defaultClassInit;

    private AbstractProperty(HasParent parentElt,T defaultValue,Class<T> defaultClassInit){
        this.parentElt = parentElt;
        this.defaultValue = defaultValue;
        this.defaultClassInit = defaultClassInit;
        hasDefaultValue =  defaultValue!=null || defaultClassInit!=null;
    }

    public AbstractProperty(HasParent parentElement){
        this(parentElement,null,null);
    }

    public AbstractProperty(HasParent parentElement,T defaultValue){
        this(parentElement,defaultValue,null);
    }

    public AbstractProperty(HasParent parentElement,Class<T> defaultClassInit){
        this(parentElement,null,defaultClassInit);
    }


    public void setParentElement(HasParent parentElement){ parentElt=parentElement;}
    public HasParent getParentElement(){return parentElt;}

    protected T getRawValue(){return value;}

    public T get(){
        if((value==null)&&(hasDefaultValue)){
            if(defaultValue!=null) {
                set(defaultValue);
            }
            else if(defaultClassInit!=null){
                try {
                    set(defaultClassInit.newInstance());
                }
                catch(InstantiationException|IllegalAccessException e){
                    throw new RuntimeException("Cannot init class "+defaultClassInit.getName()+" as default value",e);
                }
            }
        }
        return value;
    }
    public boolean set(T value) {
        if(!equalsValue(value)){
            this.value = value;
            if(parentElt!=null) {
                if (value instanceof HasParent) {
                    ((HasParent) value).setParentElement(parentElt);
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
            return equalsValue(((AbstractProperty)ref).value);
        }
        else{
            return false;
        }
    }

    public boolean equalsValue(Object value){
        if(this.value == value){
            return true;
        }
        else if(this.value !=null){
            return this.value.equals(value);
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

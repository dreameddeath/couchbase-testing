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

package com.dreameddeath.core.model.document;

import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.List;


@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE,isGetterVisibility = Visibility.NONE,setterVisibility = Visibility.NONE,creatorVisibility = Visibility.NONE)
public abstract class BaseCouchbaseDocumentElement implements HasParentDocumentElement{
    private Property<BaseCouchbaseDocumentElement> parentElt=new StandardProperty<BaseCouchbaseDocumentElement>(null);
    private List<BaseCouchbaseDocumentElement> childElementList=new ArrayList<BaseCouchbaseDocumentElement>();

    protected void addChildElement(BaseCouchbaseDocumentElement elt){
        childElementList.add(elt);
    }
    protected void removeChildElement(BaseCouchbaseDocumentElement elt){
        childElementList.remove(elt);
    }

    public <T extends BaseCouchbaseDocumentElement> List<T> getChildElementsOfType(Class<T> clazz){
        List<T> res=new ArrayList<T>();
        for(BaseCouchbaseDocumentElement child : childElementList){
            if(clazz.isAssignableFrom(child.getClass())){
                res.add((T)child);
            }
            res.addAll(child.getChildElementsOfType(clazz));
        }
        return res;
    }
    
    public BaseCouchbaseDocumentElement getParentDocumentElement() { return parentElt.get();}
    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentElt) {
        if(parentElt.get()!=null){
            parentElt.get().removeChildElement(this);
        }
        parentElt.set(parentElt);
        if(parentElt!=null) {
            parentElt.addChildElement(this);
        }
    }
    
    public  BaseCouchbaseDocument getParentDocument() {
        if(this instanceof BaseCouchbaseDocument){
            return (BaseCouchbaseDocument)this;
        }
        else if(parentElt.get() !=null){
            return parentElt.get().getParentDocument();
        }
        return null;
    }

    public void dirtyDocument(){
        BaseCouchbaseDocument doc = getParentDocument();
        if(doc!=null){
            doc.getBaseMeta().setStateDirty();
        }
    }

    public <T extends BaseCouchbaseDocumentElement> T getFirstParentOfClass(Class<T> clazz){
        if(parentElt.get()!=null){
            if(clazz.isAssignableFrom(parentElt.get().getClass())){
                return (T) (parentElt.get());
            }
            else{
                return parentElt.get().getFirstParentOfClass(clazz);
            }
        }
        return null;
    }
}

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

import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.MapDefaultValueBuilder;
import com.dreameddeath.core.model.property.MapProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class ArrayListProperty<T> extends ArrayList<T> implements ListProperty<T>,HasParentDocumentElement {
    BaseCouchbaseDocumentElement parentElt;
    public ArrayListProperty(BaseCouchbaseDocumentElement parentElement){
        parentElt=parentElement;
    }


    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentElement){
        parentElt=parentElement;
    }
    public BaseCouchbaseDocumentElement getParentDocumentElement(){return parentElt;}

    protected boolean dirtyParent(){
        BaseCouchbaseDocument rootDoc = parentElt.getParentDocument();
        if(rootDoc!=null){ rootDoc.getBaseMeta().setStateDirty();}
        return true;
    }

    @Override
    public void clear(){
        dirtyParent();
        super.clear();
    }

    @Override
    public boolean remove(Object elt){
        boolean  res = super.remove(elt);
        if(res && (elt!=null) && (elt instanceof HasParentDocumentElement)){
            ((HasParentDocumentElement) elt).setParentDocumentElement(null);
        }
        if(res){ dirtyParent(); }
        return res;
    }


    @Override
    public boolean add(T elt){
        if(elt instanceof HasParentDocumentElement){
            ((HasParentDocumentElement) elt).setParentDocumentElement(parentElt);
        }
        dirtyParent();
        return super.add(elt);
    }

    @Override
    public boolean addAll(Collection<? extends T> list){
        for(T elt : list){
            if(elt instanceof HasParentDocumentElement){
                ((HasParentDocumentElement) elt).setParentDocumentElement(parentElt);
            }
        }
        dirtyParent();
        return super.addAll(list);
    }

    public List<T> get(){
        return Collections.unmodifiableList(this);
    }

    public boolean set(Collection<T> list){
        dirtyParent();
        clear();
        return addAll(list);
    }

    public boolean set(List<T> list){
        return set((Collection<T>)list);
    }

    public static class MapValueBuilder<T> implements MapDefaultValueBuilder<List<T>>{
        public ListProperty<T> build(MapProperty<?,List<T>> map){
            BaseCouchbaseDocumentElement parent=null;
            if(map instanceof HasParentDocumentElement){
                parent = ((HasParentDocumentElement) map).getParentDocumentElement();
            }
            return new ArrayListProperty<T>(parent);
        }
    }

}

/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.model.business;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.SynchronizedLinkProperty;

import java.util.ArrayList;
import java.util.List;

public abstract class CouchbaseDocumentLink<T extends BaseCouchbaseDocument> extends BaseCouchbaseDocumentElement {
    private List<SynchronizedLinkProperty> _childLinks=new ArrayList<SynchronizedLinkProperty>();
    private Property<T>            _docObject=new ImmutableProperty<T>(null);
    @DocumentProperty("key")
    private Property<String> _key=new SynchronizedLinkProperty<String,T>(CouchbaseDocumentLink.this){
        @Override
        protected  String getRealValue(T doc){
            return doc.getBaseMeta().getKey();
        }
    };


    public void addChildSynchronizedProperty(SynchronizedLinkProperty prop){
        _childLinks.add(prop);
    }

    public final String getKey(){ return _key.get();}
    public final void setKey(String key){ _key.set(key); }
    

    public T getLinkedObjectFromCache(){
        return _docObject.get();
    }

    public T getLinkedObject() throws DaoException,StorageException{
        if((_docObject.get()==null)){
            if(_key==null){
                ///TODO throw an error
            }
            else{
                BaseCouchbaseDocument parentDoc = getParentDocument();
                if((parentDoc!=null) && parentDoc.getBaseMeta().getSession()!=null){
                    _docObject.set((T)parentDoc.getBaseMeta().getSession().get(_key.get()));
                }
            }
        }
        return _docObject.get();
    }
    
    public void setLinkedObject(T docObj){ 
        _docObject.set(docObj);
        if(docObj instanceof CouchbaseDocument) {
            ((CouchbaseDocument)docObj).getMeta().addReverseLink(this);
        }
    }
    
    
    public CouchbaseDocumentLink(){}
    public CouchbaseDocumentLink(T targetDoc){
        if(targetDoc.getBaseMeta().getKey()!=null){
            setKey(targetDoc.getBaseMeta().getKey());
        }
        setLinkedObject(targetDoc);
    }
    
    public CouchbaseDocumentLink(CouchbaseDocumentLink<T> srcLink){
        setKey(srcLink.getKey());
        setLinkedObject(srcLink._docObject.get());
    }
    
    
    @Override
    public boolean equals(Object target){
        if(target==null){
            return false;
        }
        else if(this == target){
            return true;
        }
        else if(target instanceof CouchbaseDocumentLink){
            CouchbaseDocumentLink targetLnk=(CouchbaseDocumentLink) target;
            if((_key!=null) && _key.equals(targetLnk._key)){
                return true;
            }
            else if((_docObject!=null)&& _docObject.equals(targetLnk._docObject)){
                return true;
            }
        }

        return false;
    }
    
    @Override
    public String toString(){
        return "key : "+getKey();
    }

    public void syncFields(){
        for(SynchronizedLinkProperty prop:_childLinks){
            prop.sync();
        }
    }

}

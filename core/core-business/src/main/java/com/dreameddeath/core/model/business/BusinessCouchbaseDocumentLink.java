package com.dreameddeath.core.model.business;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.SynchronizedLinkProperty;
import com.dreameddeath.core.session.ICouchbaseSession;

import java.util.ArrayList;
import java.util.List;

public abstract class BusinessCouchbaseDocumentLink<T extends CouchbaseDocument> extends CouchbaseDocumentElement {
    private List<SynchronizedLinkProperty> _childLinks=new ArrayList<SynchronizedLinkProperty>();
    private Property<T>            _docObject=new ImmutableProperty<T>(null);
    @DocumentProperty("key")
    private Property<String> _key=new SynchronizedLinkProperty<String,T>(BusinessCouchbaseDocumentLink.this){
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

    public T getLinkedObject(ICouchbaseSession session)throws DaoException,StorageException{
        if(getLinkedObjectFromCache()==null){
            setLinkedObject((T)session.get(getKey()));
        }
        return getLinkedObjectFromCache();
    }

    public void setLinkedObject(T docObj){
        _docObject.set(docObj);
        if(docObj instanceof BusinessCouchbaseDocument) {
            ((BusinessCouchbaseDocument)docObj).getMeta().addReverseLink(this);
        }
    }


    public BusinessCouchbaseDocumentLink(){}
    public BusinessCouchbaseDocumentLink(T targetDoc){
        if(targetDoc.getBaseMeta().getKey()!=null){
            setKey(targetDoc.getBaseMeta().getKey());
        }
        setLinkedObject(targetDoc);
    }
    
    public BusinessCouchbaseDocumentLink(BusinessCouchbaseDocumentLink<T> srcLink){
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
        else if(target instanceof BusinessCouchbaseDocumentLink){
            BusinessCouchbaseDocumentLink targetLnk=(BusinessCouchbaseDocumentLink) target;
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

package com.dreameddeath.common.model.property;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentLink;

public abstract class SynchronizedLinkProperty<T,TDOC extends CouchbaseDocument> extends StandardProperty<T>  {
    CouchbaseDocumentLink<TDOC> _parentLink;

    public SynchronizedLinkProperty(CouchbaseDocumentLink<TDOC> parentLink){
        super(parentLink);
        parentLink.addChildSynchronizedProperty(this);
        _parentLink=parentLink;
    }
    
    protected abstract T getRealValue(TDOC doc);

    @Override
    public final T get(){ 
        if(_parentLink.getLinkedObject(true)!=null){
            set(getRealValue(_parentLink.getLinkedObject()));
        }
        return super.get();
    }

    public void sync(){
        get();
    }
}

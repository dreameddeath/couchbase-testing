package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.business.CouchbaseDocumentLink;

public abstract class SynchronizedLinkProperty<T,TDOC extends BaseCouchbaseDocument> extends StandardProperty<T> {
    CouchbaseDocumentLink<TDOC> _parentLink;

    public SynchronizedLinkProperty(CouchbaseDocumentLink<TDOC> parentLink){
        super(parentLink);
        parentLink.addChildSynchronizedProperty(this);
        _parentLink=parentLink;
    }
    
    protected abstract T getRealValue(TDOC doc);

    @Override
    public final T get(){ 
        if(_parentLink.getLinkedObjectFromCache()!=null){
            set(getRealValue(_parentLink.getLinkedObjectFromCache()));
        }
        return super.get();
    }

    public void sync(){
        get();
    }
}

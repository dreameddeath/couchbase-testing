package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocumentLink;

public abstract class SynchronizedLinkProperty<T,TDOC extends CouchbaseDocument> extends StandardProperty<T> {
    BusinessCouchbaseDocumentLink<TDOC> _parentLink;

    public SynchronizedLinkProperty(BusinessCouchbaseDocumentLink<TDOC> parentLink){
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

package com.dreameddeath.core.model.property;

import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.util.Collection;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public interface CollectionProperty<T> extends Collection<T>{
    public boolean set(Collection<T> list);
}

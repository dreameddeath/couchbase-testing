package com.dreameddeath.common.model;

import java.util.List;
import com.dreameddeath.common.annotation.CouchbaseCollectionField;

@CouchbaseCollectionField
public interface CouchbaseDocumentList<T> extends List<T>{
    
}

package com.dreameddeath.core.event;

import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.document.CouchbaseDocument;

public class FieldUpdateEvent{
    private CouchbaseDocument _rootDoc;
    private BaseCouchbaseDocumentElement _elementDoc;
    private String  _fieldFullPath;
    private String  _fieldPath;
    private Object  _newValue;
    
    
}
package com.dreameddeath.core.event;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

public class FieldUpdateEvent{
    private CouchbaseDocument _rootDoc;
    private CouchbaseDocumentElement _elementDoc;
    private String  _fieldFullPath;
    private String  _fieldPath;
    private Object  _newValue;
    
    
}
package com.dreameddeath.common.event;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class FieldUpdateEvent{
    private CouchbaseDocument _rootDoc;
    private CouchbaseDocumentElement _elementDoc;
    private String  _fieldFullPath;
    private String  _fieldPath;
    private Object  _newValue;
    
    
}
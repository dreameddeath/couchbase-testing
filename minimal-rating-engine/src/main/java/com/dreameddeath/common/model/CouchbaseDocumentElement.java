package com.dreameddeath.common.model;

import java.util.HashSet;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;


@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)
public abstract class CouchbaseDocumentElement{
    private CouchbaseDocument _parent;
    
    public CouchbaseDocument getParentDocument() { return _parent;}
    public void setParentDocument(CouchbaseDocument parent) { _parent=parent;}
}

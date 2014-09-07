package com.dreameddeath.core.model.property;

import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public interface HasParentDocumentElement {
    public CouchbaseDocumentElement getParentDocumentElement();
    public void setParentDocumentElement(CouchbaseDocumentElement parentDocumentElement);
}
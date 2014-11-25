package com.dreameddeath.core.model.property;

import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public interface HasParentDocumentElement {
    public BaseCouchbaseDocumentElement getParentDocumentElement();
    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentDocumentElement);
}
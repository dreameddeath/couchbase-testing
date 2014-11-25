package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

/**
 * Created by CEAJ8230 on 22/09/2014.
 */
public class DocumentDeleteRequest extends CouchbaseDocumentElement {
    @DocumentProperty("key")
    public String key;
    @DocumentProperty("expiration")
    public Integer expiration;
    @DocumentProperty("withChildren")
    public Boolean withChildren;
}

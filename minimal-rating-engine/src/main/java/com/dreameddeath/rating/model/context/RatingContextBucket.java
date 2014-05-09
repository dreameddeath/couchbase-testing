package com.dreameddeath.rating.model.context;

import com.dreameddeath.common.model.CouchbaseDocumentElement;
import com.dreameddeath.common.annotation.DocumentProperty;

public class RatingContextBucket extends CouchbaseDocumentElement{
    @DocumentProperty("code")
    private String _code;
    
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
}
package com.dreameddeath.rating.model.context;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.CouchbaseDocumentElement;
import com.dreameddeath.common.model.property.StandardProperty;
import com.dreameddeath.common.model.property.Property;

public class RatingContextBucket extends CouchbaseDocumentElement{
    @DocumentProperty("code")
    private Property<String> _code=new StandardProperty<String>(RatingContextBucket.this);
    
    public String getCode(){ return _code.get();}
    public void setCode(String code){ this._code.set(code); }
}
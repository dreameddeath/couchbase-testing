package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextBucket extends CouchbaseDocumentElement{
    @JsonProperty("code")
    private String _code;
    
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
}
package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextBucket extends CouchbaseDocumentElement{
    private String _code;
    
    @JsonProperty("code")
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
}
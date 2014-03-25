package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class RatingContextBucket{
    private String _code;
    
    @JsonProperty("code")
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
 
    
}
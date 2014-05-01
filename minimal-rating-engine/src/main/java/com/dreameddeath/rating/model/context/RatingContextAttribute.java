package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextAttribute extends CouchbaseDocumentElement{
    private String _code;
    private List<RatingContextAttributeValue> _values=new CouchbaseDocumentArrayList<RatingContextAttributeValue>(RatingContextAttribute.this);
    
    @JsonProperty("code")
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
 
    @JsonProperty("values")
    public List<RatingContextAttributeValue> getValues(){return Collections.unmodifiableList(_values);}
    public void setValues(List<RatingContextAttributeValue> values){ _values.clear();_values.addAll(values); }
    public void addValues(List<RatingContextAttributeValue> values){ _values.addAll(values); }
    public void addValue(RatingContextAttributeValue value){ _values.add(value); }

 
}
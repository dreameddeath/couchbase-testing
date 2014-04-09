package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.CouchbaseDocumentElement;

public class RatingContextAttribute extends CouchbaseDocumentElement{
    @JsonProperty("code")
    private String _code;
    @JsonProperty("values")
    private List<RatingContextAttributeValue> _values=new CouchbaseDocumentArrayList<RatingContextAttributeValue>(RatingContextAttribute.this);
    
    public String getCode(){ return _code;}
    public void setCode(String code){ this._code = code; }
 
    public List<RatingContextAttributeValue> getValues(){return Collections.unmodifiableList(_values);}
    public void setValues(List<RatingContextAttributeValue> values){ _values.clear();_values.addAll(values); }
    public void addValues(List<RatingContextAttributeValue> values){ _values.addAll(values); }
    public void addValue(RatingContextAttributeValue value){ _values.add(value); }

 
}
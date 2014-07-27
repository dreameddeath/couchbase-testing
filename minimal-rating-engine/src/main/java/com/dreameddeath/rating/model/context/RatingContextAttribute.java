package com.dreameddeath.rating.model.context;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocumentArrayList;
import com.dreameddeath.common.model.document.CouchbaseDocumentElement;
import com.dreameddeath.common.model.property.StandardProperty;
import com.dreameddeath.common.model.property.Property;

import java.util.Collections;
import java.util.List;

public class RatingContextAttribute extends CouchbaseDocumentElement{
    @DocumentProperty("code")
    private Property<String> _code=new StandardProperty<String>(RatingContextAttribute.this);
    @DocumentProperty("values")
    private List<RatingContextAttributeValue> _values=new CouchbaseDocumentArrayList<RatingContextAttributeValue>(RatingContextAttribute.this);
    
    public String getCode(){ return _code.get();}
    public void setCode(String code){ _code.set(code); }

    public List<RatingContextAttributeValue> getValues(){return Collections.unmodifiableList(_values);}
    public void setValues(List<RatingContextAttributeValue> values){ _values.clear();_values.addAll(values); }
    public void addValues(List<RatingContextAttributeValue> values){ _values.addAll(values); }
    public void addValue(RatingContextAttributeValue value){ _values.add(value); }

 
}
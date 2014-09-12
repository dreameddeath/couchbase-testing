package com.dreameddeath.rating.model.context;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.model.property.Property;

import java.util.Collections;
import java.util.List;

public class RatingContextAttribute extends BaseCouchbaseDocumentElement {
    @DocumentProperty("code")
    private Property<String> _code=new StandardProperty<String>(RatingContextAttribute.this);
    @DocumentProperty("values")
    private List<RatingContextAttributeValue> _values=new ArrayListProperty<RatingContextAttributeValue>(RatingContextAttribute.this);
    
    public String getCode(){ return _code.get();}
    public void setCode(String code){ _code.set(code); }

    public List<RatingContextAttributeValue> getValues(){return Collections.unmodifiableList(_values);}
    public void setValues(List<RatingContextAttributeValue> values){ _values.clear();_values.addAll(values); }
    public void addValues(List<RatingContextAttributeValue> values){ _values.addAll(values); }
    public void addValue(RatingContextAttributeValue value){ _values.add(value); }

 
}
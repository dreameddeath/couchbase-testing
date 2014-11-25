package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.NumericProperty;

/**
 * Created by ceaj8230 on 14/08/2014.
 */
public class StandardLongProperty extends StandardProperty<Long> implements NumericProperty<Long> {
    public StandardLongProperty(BaseCouchbaseDocumentElement parent){  super(parent);}
    public StandardLongProperty(BaseCouchbaseDocumentElement parent, Number defaultValue){super(parent,defaultValue.longValue());}

    public StandardLongProperty inc(Number byVal){_value=get()+byVal.longValue();return this;}
    public StandardLongProperty dec(Number byVal){_value=get()-byVal.longValue();return this;}
    public StandardLongProperty mul(Number byVal){_value=get()+byVal.longValue();return this;}
    public StandardLongProperty div(Number byVal){_value=get()+byVal.longValue();return this;}
}

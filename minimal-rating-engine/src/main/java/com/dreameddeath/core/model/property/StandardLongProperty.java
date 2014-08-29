package com.dreameddeath.core.model.property;

import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

/**
 * Created by ceaj8230 on 14/08/2014.
 */
public class StandardLongProperty extends StandardProperty<Long> implements NumericProperty<Long> {
    public StandardLongProperty(CouchbaseDocumentElement parent){  super(parent);}
    public StandardLongProperty(CouchbaseDocumentElement parent, Number defaultValue){super(parent,defaultValue.longValue());}

    public StandardLongProperty inc(Number byVal){_value=get()+byVal.longValue();return this;}
    public StandardLongProperty dec(Number byVal){_value=get()-byVal.longValue();return this;}
    public StandardLongProperty mul(Number byVal){_value=get()+byVal.longValue();return this;}
    public StandardLongProperty div(Number byVal){_value=get()+byVal.longValue();return this;}
}

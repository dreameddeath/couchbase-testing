package com.dreameddeath.common.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;

/**
 * Created by ceaj8230 on 11/08/2014.
 */
public class PostalAddress extends Address {
    /**
     *  name : name of the receiver
     */
    @DocumentProperty("name")
    private Property<String> _name = new StandardProperty<String>(PostalAddress.this);

    // name accessors
    public String getName() { return _name.get(); }
    public void setName(String val) { _name.set(val); }
}

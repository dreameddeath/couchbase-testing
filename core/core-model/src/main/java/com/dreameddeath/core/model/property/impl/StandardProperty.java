package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.property.HasParent;

/**
 * Created by Christophe Jeunesse on 09/05/2014.
 */
public class StandardProperty<T> extends AbstractProperty<T> {

    public StandardProperty(HasParent parent){
        super(parent);
    }

    public StandardProperty(HasParent parent, T defaultValue){
        super(parent,defaultValue);
    }
}

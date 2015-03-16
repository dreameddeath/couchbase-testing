package com.dreameddeath.core.config.impl;

import com.dreameddeath.core.config.AbstractProperty;
import com.dreameddeath.core.config.PropertyChangedCallback;

/**
 * Created by CEAJ8230 on 03/02/2015.
 */
public class BooleanProperty extends AbstractProperty<Boolean> {

    public BooleanProperty(String name, Boolean defaultValue) {
        super(new BooleanExtendedProperty(name,defaultValue));
    }

    public BooleanProperty(String name, Boolean defaultValue,PropertyChangedCallback<Boolean> callback) {
        super(new BooleanExtendedProperty(name,defaultValue),callback);
    }


    public boolean get(){return getValue();}

    protected static class BooleanExtendedProperty extends ExtendedPropertyWrapper<Boolean>{
        public BooleanExtendedProperty(String name,Boolean defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Boolean getValue() {
            return prop.getBoolean(defaultValue);
        }
    }
}

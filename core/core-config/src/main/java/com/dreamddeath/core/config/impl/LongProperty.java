package com.dreamddeath.core.config.impl;

import com.dreamddeath.core.config.AbstractProperty;
import com.dreamddeath.core.config.PropertyChangedCallback;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class LongProperty extends AbstractProperty<Long> {

    public LongProperty(String name, Long defaultValue) {
        super(new LongExtendedProperty(name,defaultValue));
    }
    public LongProperty(String name, Long defaultValue,PropertyChangedCallback<Long> callback) {
        super(new LongExtendedProperty(name,defaultValue),callback);
    }

    public long get(){return getValue();}

    protected static class LongExtendedProperty extends AbstractProperty.ExtendedPropertyWrapper<Long> {
        public LongExtendedProperty(String name,Long defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Long getValue() {
            return prop.getLong(defaultValue);
        }
    }
}

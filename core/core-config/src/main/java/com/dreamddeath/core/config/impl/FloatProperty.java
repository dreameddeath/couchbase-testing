package com.dreamddeath.core.config.impl;

import com.dreamddeath.core.config.AbstractProperty;
import com.dreamddeath.core.config.PropertyChangedCallback;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class FloatProperty extends AbstractProperty<Float> {

    public FloatProperty(String name, Float defaultValue) {
            super(new FloatExtendedProperty(name,defaultValue));
            }
    public FloatProperty(String name, Float defaultValue,PropertyChangedCallback<Float> callback) {
        super(new FloatExtendedProperty(name,defaultValue),callback);
    }

    public float get(){return getValue();}

    protected static class FloatExtendedProperty extends AbstractProperty.ExtendedPropertyWrapper<Float> {
        public FloatExtendedProperty(String name,Float defaultValue) {
            super(name,defaultValue);
        }
        @Override
        public Float getValue() {
            return prop.getFloat(defaultValue);
        }
    }
}

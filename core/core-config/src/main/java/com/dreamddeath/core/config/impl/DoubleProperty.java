package com.dreamddeath.core.config.impl;

import com.dreamddeath.core.config.AbstractProperty;
import com.dreamddeath.core.config.PropertyChangedCallback;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class DoubleProperty extends AbstractProperty<Double> {

    public DoubleProperty(String name, Double defaultValue) {
        super(new DoubleExtendedProperty(name,defaultValue));
    }

    public DoubleProperty(String name, Double defaultValue,PropertyChangedCallback<Double>callback) {
        super(new DoubleExtendedProperty(name,defaultValue),callback);
    }

    public double get(){return getValue();}

    protected static class DoubleExtendedProperty extends ExtendedPropertyWrapper<Double>{
        public DoubleExtendedProperty(String name,Double defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Double getValue() {
            return prop.getDouble(defaultValue);
        }
    }
}

package com.dreamddeath.core.config.impl;

import com.dreamddeath.core.config.AbstractProperty;
import com.dreamddeath.core.config.PropertyChangedCallback;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class IntProperty extends AbstractProperty<Integer> {

    public IntProperty(String name, Integer defaultValue) {
        super(new IntegerExtendedProperty(name,defaultValue));
    }

    public IntProperty(String name, Integer defaultValue,PropertyChangedCallback<Integer> callback) {
        super(new IntegerExtendedProperty(name,defaultValue),callback);
    }


    public int get(){return getValue();}

    protected static class IntegerExtendedProperty extends AbstractProperty.ExtendedPropertyWrapper<Integer> {
        public IntegerExtendedProperty(String name,Integer defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Integer getValue() {
            return prop.getInteger(defaultValue);
        }
    }}

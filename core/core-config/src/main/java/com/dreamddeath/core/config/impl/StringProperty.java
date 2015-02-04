package com.dreamddeath.core.config.impl;

import com.dreamddeath.core.config.AbstractProperty;
import com.dreamddeath.core.config.PropertyChangedCallback;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class StringProperty extends AbstractProperty<String> {

    public StringProperty(String name, String defaultValue) {
        super(new StringExtendedProperty(name,defaultValue));
    }
    public StringProperty(String name, String defaultValue,PropertyChangedCallback<String> callback) {
        super(new StringExtendedProperty(name,defaultValue),callback);
    }

    public String get(){return getValue();}

    protected static class StringExtendedProperty extends AbstractProperty.ExtendedPropertyWrapper<String> {
        public StringExtendedProperty(String name,String defaultValue){
            super(name,defaultValue);
        }
        @Override
        public String getValue() {
            return prop.getString(defaultValue);
        }
    }}

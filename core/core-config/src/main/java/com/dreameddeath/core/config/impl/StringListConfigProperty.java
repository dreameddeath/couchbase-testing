package com.dreameddeath.core.config.impl;

import com.dreameddeath.core.config.AbstractConfigListProperty;
import com.dreameddeath.core.config.ConfigPropertyChangedCallback;
import com.google.common.base.Splitter;

import java.util.List;

/**
 * Created by CEAJ8230 on 23/05/2015.
 */
public class StringListConfigProperty extends AbstractConfigListProperty<String> {
    public StringListConfigProperty(String name, String delimiterRegexp, String defaultValue) {
        super(name, delimiterRegexp, defaultValue);
    }

    public StringListConfigProperty(String name, String delimiterRegexp, AbstractConfigListProperty<String> defaultValueRef) {
        super(name, delimiterRegexp, defaultValueRef);
    }

    public StringListConfigProperty(String name, String delimiterRegexp, String defaultValue,ConfigPropertyChangedCallback<List<String>> callback) {
        super(name, delimiterRegexp, defaultValue,callback);
    }

    public StringListConfigProperty(String name, String delimiterRegexp, AbstractConfigListProperty<String> defaultValueRef,ConfigPropertyChangedCallback<List<String>> callback) {
        super(name, delimiterRegexp, defaultValueRef,callback);
    }



    public StringListConfigProperty(String name, String defaultValue) {
        super(name, defaultValue);
    }

    public StringListConfigProperty(String name, String defaultValue,ConfigPropertyChangedCallback<List<String>> callback) {
        super(name, defaultValue,callback);
    }




    public StringListConfigProperty(String name, AbstractConfigListProperty<String> defaultValueRef) {
        super(name, defaultValueRef);
    }

    public StringListConfigProperty(String name, AbstractConfigListProperty<String> defaultValueRef,ConfigPropertyChangedCallback<List<String>> callback) {
        super(name, defaultValueRef,callback);
    }

    @Override
    protected List<String> splitValue(Splitter splitter, String value) {
        return splitter.splitToList(value);
    }
}

package com.dreameddeath.core.config.internal;

import com.netflix.config.PropertyWrapper;

/**
 * Created by CEAJ8230 on 22/05/2015.
 */
public abstract class ExtendedPropertyWrapper<T> extends PropertyWrapper<T> {
    private T _oldValue = null;

    public ExtendedPropertyWrapper(String name, T defaultValue) {
        super(name, defaultValue);
    }

    @Override
    protected void propertyChanged(T newValue) {
        _oldValue = getValue();
    }

    public T getOldValue() {
        return _oldValue;
    }
}

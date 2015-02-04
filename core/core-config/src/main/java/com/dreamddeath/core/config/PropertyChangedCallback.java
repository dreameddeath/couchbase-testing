package com.dreamddeath.core.config;

/**
 * Created by CEAJ8230 on 03/02/2015.
 */
public interface PropertyChangedCallback<T> {
    public void onChange(IProperty<T> prop,T oldValue,T newValue);
}

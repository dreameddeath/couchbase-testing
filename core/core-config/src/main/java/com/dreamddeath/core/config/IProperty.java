package com.dreamddeath.core.config;

import org.joda.time.DateTime;

/**
 * Created by CEAJ8230 on 03/02/2015.
 */
public interface IProperty<T> {
    T getValue();
    T getDefaultValue();
    String getName();
    DateTime getLastChangedDate();
    void addCallback(PropertyChangedCallback<T> callback);
    void removeAllCallbacks();
}

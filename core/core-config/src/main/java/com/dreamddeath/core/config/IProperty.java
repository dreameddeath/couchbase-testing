package com.dreamddeath.core.config;

import com.dreamddeath.core.exception.config.PropertyValueNotFound;
import org.joda.time.DateTime;

/**
 * Created by CEAJ8230 on 03/02/2015.
 */
public interface IProperty<T> {
    T getValue();
    T getMandatoryValue(String errorMessage) throws PropertyValueNotFound;
    T getDefaultValue();
    String getName();
    DateTime getLastChangedDate();
    void addCallback(PropertyChangedCallback<T> callback);
    void removeAllCallbacks();
}

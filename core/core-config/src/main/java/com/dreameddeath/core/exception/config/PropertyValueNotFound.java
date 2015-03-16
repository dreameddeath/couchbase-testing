package com.dreameddeath.core.exception.config;

import com.dreameddeath.core.config.IProperty;

/**
 * Created by CEAJ8230 on 05/02/2015.
 */
public class PropertyValueNotFound extends Exception {
    IProperty _property;

    public PropertyValueNotFound(IProperty prop,String message){
        super(message);
        _property = prop;
    }

    @Override
    public String getMessage(){
        return "The property <"+_property.getName()+"> value hasn't been found. The error message :\n"+super.getMessage();
    }
}

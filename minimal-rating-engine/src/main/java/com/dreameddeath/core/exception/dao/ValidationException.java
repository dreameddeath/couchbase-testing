package com.dreameddeath.core.exception.dao;

import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.lang.reflect.AccessibleObject;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class ValidationException extends Exception {
    CouchbaseDocumentElement _docElt;
    AccessibleObject _field;
    Object _value;

    public ValidationException(CouchbaseDocumentElement docElt,AccessibleObject field,String message){
        super(message);
        _docElt = docElt;
        _field = field;
    }

    public ValidationException(CouchbaseDocumentElement docElt,AccessibleObject field,String message,Throwable e){
        super(message,e);
        _docElt = docElt;
        _field = field;
    }

    public ValidationException(CouchbaseDocumentElement docElt,AccessibleObject field,Throwable e){
        super(e);
        _docElt = docElt;
        _field = field;
    }


    public ValidationException(CouchbaseDocumentElement docElt,AccessibleObject field,Object value,String message){
        super(message);
        _docElt = docElt;
        _field = field;
        _value = value;
    }

    public ValidationException(CouchbaseDocumentElement docElt,AccessibleObject field,Object value,String message,Throwable e){
        super(message,e);
        _docElt = docElt;
        _field = field;
        _value = value;
    }

    public ValidationException(CouchbaseDocumentElement docElt,AccessibleObject field,Object value,Throwable e){
        super(e);
        _docElt = docElt;
        _field = field;
        _value = value;
    }
}

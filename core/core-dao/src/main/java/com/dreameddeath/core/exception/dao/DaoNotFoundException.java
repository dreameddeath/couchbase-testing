package com.dreameddeath.core.exception.dao;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class DaoNotFoundException extends DaoException {
    public DaoNotFoundException(Class docClass){
        super("The dao for doc Class "+docClass.getName()+" hasn't been found");
    }

    public DaoNotFoundException(Class docClass,String viewName){
        super("The view <"+viewName+"> dao for doc Class "+docClass.getName()+" hasn't been found");
    }

    public DaoNotFoundException(CouchbaseDocument doc){
        this(doc.getClass());
    }

    public DaoNotFoundException(String key, Type type){
        super("The dao for "+type.toString()+ " with key <"+key+"> hasn't been found");
    }

    public enum Type{
        DOC,
        COUNTER,
        KEY
    }
}

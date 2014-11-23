package com.dreameddeath.core.exception.dao;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class DaoNotFoundException extends DaoException {
    public DaoNotFoundException(Class docClass){
        super("The dao for doc Class "+docClass.getName()+" hasn't been found");
    }

    public DaoNotFoundException(RawCouchbaseDocument doc){
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

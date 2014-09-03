package com.dreameddeath.core.exception.dao;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class ReadOnlyException extends DaoException {
    public ReadOnlyException(CouchbaseDocument doc){
        super("Trying to update the document  <"+doc.getClass().getName()+">"+((doc.getKey()!=null)?" withKey <"+doc.getKey()+">":"")+" while being in a read only session");
    }

    public ReadOnlyException(Class docClass){
        super("Trying to update the document  <"+docClass.getName()+"> while being in a read only session");
    }

    public ReadOnlyException(String counterKey){
        super("Trying to update the counter key <"+counterKey+"> while being in a read only session");
    }
}

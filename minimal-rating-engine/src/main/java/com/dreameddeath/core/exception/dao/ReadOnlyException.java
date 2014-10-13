package com.dreameddeath.core.exception.dao;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class ReadOnlyException extends DaoException {
    public ReadOnlyException(BaseCouchbaseDocument doc){
        super("Trying to update the document  <"+doc.getClass().getName()+">"+((doc.getBaseMeta().getKey()!=null)?" withKey <"+doc.getBaseMeta().getKey()+">":"")+" while being in a read only session");
    }

    public ReadOnlyException(Class docClass){
        super("Trying to update the document  <"+docClass.getName()+"> while being in a read only session");
    }

    public ReadOnlyException(CouchbaseUniqueKey uniqueKey){
        super("Trying to update the unique <"+uniqueKey.getBaseMeta().getKey()+"> while being in a read only session");
    }

    public ReadOnlyException(String counterKey){
        super("Trying to update the counter key <"+counterKey+"> while being in a read only session");
    }
}

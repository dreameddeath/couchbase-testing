package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.exception.validation.AbstractValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.session.ICouchbaseSession;

import java.util.Set;

public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument> extends BaseCouchbaseDocumentWithKeyPatternDao<T> {

    protected  void updateRevision(T obj){
        obj.incDocRevision();
        obj.updateDocLastModDate();
    }


    public T create(ICouchbaseSession session,T obj,boolean isCalcOnly) throws AbstractValidationException,DaoException,StorageException{
        return super.create(session,obj,isCalcOnly);
    }

    public T update(ICouchbaseSession session,T obj,boolean isCalcOnly) throws AbstractValidationException,DaoException,StorageException{
        updateRevision(obj);
        Set<String> keysToRemove=obj.getRemovedUniqueKeys();
        super.update(session,obj, isCalcOnly);
        for(String key :keysToRemove){
            session.removeUniqueKey(key);
        }
        return obj;
    }

    //Should only be used through DeletionJob
    public T delete(ICouchbaseSession session,T doc,boolean isCalcOnly) throws DaoException,StorageException{
        Set<String> keysToRemove=doc.getRemovedUniqueKeys();
        super.delete(session,doc,isCalcOnly);
        for(String key :keysToRemove){
            session.removeUniqueKey(key);
        }
        return doc;
    }


    public enum AccessRight{
        READ,
        CREATE,
        UPDATE,
        DELETE
    }
}
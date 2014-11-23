package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.exception.validation.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.session.ICouchbaseSession;

import java.util.Set;

public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument> extends BaseCouchbaseDocumentWithKeyPatternDao<T> {

    protected  void updateRevision(ICouchbaseSession session,T obj){
        obj.incDocRevision(session);
        obj.updateDocLastModDate(session);
    }


    @Override
    public T create(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        return super.create(session,obj,isCalcOnly);
    }

    @Override
    public T update(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        updateRevision(session,obj);
        Set<String> keysToRemove=obj.getRemovedUniqueKeys();
        super.update(session,obj, isCalcOnly);
        for(String key :keysToRemove){
            session.removeUniqueKey(key);
        }
        return obj;
    }

    //Should only be used through DeletionJob
    @Override
    public T delete(ICouchbaseSession session,T doc,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        Set<String> keysToRemove=doc.getRemovedUniqueKeys();
        super.delete(session,doc,isCalcOnly);
        for(String key :keysToRemove){
            session.removeUniqueKey(key);
        }
        return doc;
    }
}
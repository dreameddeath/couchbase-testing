package com.dreameddeath.core.dao.business;

import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.exception.validation.ValidationException;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.session.ICouchbaseSession;

import java.util.Set;

@DaoForClass(BusinessCouchbaseDocument.class)
public abstract class BusinessCouchbaseDocumentDao<T extends BusinessCouchbaseDocument> extends CouchbaseDocumentWithKeyPatternDao<T> {

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
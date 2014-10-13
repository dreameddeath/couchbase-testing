package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentDao;
import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.validation.Validator;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentLink;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument> extends BaseCouchbaseDocumentWithKeyPatternDao<T> {

    protected  void updateRevision(T obj){
        obj.incDocRevision();
        obj.updateDocLastModDate();
    }

    public CouchbaseDocumentDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }


    public T validate(T obj) throws ValidationException{
        Validator<T> validator = getDaoFactory().getValidatorFactory().getValidator(obj);
        if(validator!=null){
            validator.validate(obj,null);
        }
        return obj;
    }


    public T create(T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        validate(obj);
        return super.create(obj,isCalcOnly);
    }

    /*public void cleanRemovedUniqueKey(T obj) throws DaoException,StorageException{
        for(String key :obj.getRemovedUniqueKeys()){
            obj.getBaseMeta().getSession().removeUniqueKey(key);
        }
    }*/

    public T update(T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        validate(obj);
        updateRevision(obj);
        Set<String> keysToRemove=obj.getRemovedUniqueKeys();
        super.update(obj, isCalcOnly);
        for(String key :keysToRemove){
            obj.getBaseMeta().getSession().removeUniqueKey(key);
        }
        //cleanRemovedUniqueKey(obj);
        return obj;
    }

    public T getLinkObj(CouchbaseDocumentLink<T> link) throws StorageException,DaoException{
        if(link.getLinkedObjectFromCache()!=null) { return link.getLinkedObjectFromCache(); }
        T result = get(link.getKey());
        link.setLinkedObject(result);
        return result;
    }

    //Should only be used through DeletionJob
    public T delete(T doc,boolean isCalcOnly) throws DaoException,StorageException{
        Set<String> keysToRemove=doc.getRemovedUniqueKeys();
        super.delete(doc,isCalcOnly);
        for(String key :keysToRemove){
            doc.getBaseMeta().getSession().removeUniqueKey(key);
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
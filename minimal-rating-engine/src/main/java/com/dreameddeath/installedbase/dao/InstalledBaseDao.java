package com.dreameddeath.installedbase.dao;

import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.model.document.BucketDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;
import com.dreameddeath.installedbase.model.common.InstalledBase;

/**
 * Created by ceaj8230 on 31/08/2014.
 */
public class InstalledBaseDao extends CouchbaseDocumentDaoWithUID<InstalledBase> {
    public static final String INSTALLED_BASE_FMT_UID="%010d";
    public static final String INSTALLED_BASE_CNT_KEY="instBase/cnt";
    public static final String INSTALLED_BASE_FMT_KEY="instBase/%010d";
    public static final String INSTALLED_BASE_KEY_PATTERN="instBase/\\d{10}";
    public static final String INSTALLED_BASE_CNT_PATTERN="instBase/cnt";

    public static class LocalBucketDocument extends BucketDocument<InstalledBase> {
        public LocalBucketDocument(InstalledBase obj){super(obj);}
    }

    private static GenericJacksonTranscoder<InstalledBase> _tc = new GenericJacksonTranscoder<InstalledBase>(InstalledBase.class,LocalBucketDocument.class);

    @Override
    public GenericTranscoder<InstalledBase> getTranscoder(){
        return _tc;
    }

    public InstalledBaseDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(INSTALLED_BASE_CNT_PATTERN).withDefaultValue(1L));
    }

    @Override
    public void buildKey(InstalledBase obj) throws DaoException{
        long result = obj.getMeta().getSession().incrCounter(INSTALLED_BASE_CNT_KEY, 1);
        obj.setUid(String.format(INSTALLED_BASE_FMT_UID,result));
        obj.getMeta().setKey(String.format(INSTALLED_BASE_FMT_KEY, result));
    }

    @Override
    public String getKeyPattern(){
        return INSTALLED_BASE_KEY_PATTERN;
    }
    @Override
    public String getKeyFromUID(String uid){ return String.format(INSTALLED_BASE_FMT_KEY, Long.parseLong(uid));}
}

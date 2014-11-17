package com.dreameddeath.billing.dao;


import com.dreameddeath.billing.model.installedbase.BillingInstalledBase;
import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.model.common.BucketDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;


/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBaseDao extends CouchbaseDocumentDao<BillingInstalledBase> {
    public static final String BA_BASE_CNT_KEY="%s/base/cnt";
    public static final String BA_BASE_FMT_KEY="%s/base/%d";
    public static final String BA_BASE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/\\d+";
    public static final String BA_BASE_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/cnt";

    public static class LocalBucketDocument extends BucketDocument<BillingInstalledBase> {
        public LocalBucketDocument(BillingInstalledBase baInstBase){super(baInstBase);}
    }

    private static GenericJacksonTranscoder<BillingInstalledBase> _tc = new GenericJacksonTranscoder<BillingInstalledBase>(BillingInstalledBase.class,LocalBucketDocument.class);

    public GenericTranscoder<BillingInstalledBase> getTranscoder(){ return _tc; }

    public BillingInstalledBaseDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(BA_BASE_CNT_PATTERN).withDefaultValue(1L));
    }

    public void buildKey(BillingInstalledBase obj) throws DaoException{
        long result = obj.getBaseMeta().getSession().incrCounter(String.format(BA_BASE_CNT_KEY, obj.getBaLink().getKey()), 1);
        obj.getBaseMeta().setKey(String.format(BA_BASE_FMT_KEY, obj.getBaLink().getKey(), result));
    }

    public String getKeyPattern(){
        return BA_BASE_KEY_PATTERN;
    }
}
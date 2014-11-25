package com.dreameddeath.billing.dao;


import com.dreameddeath.billing.model.installedbase.BillingInstalledBase;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;


/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBaseDao extends BusinessCouchbaseDocumentDao<BillingInstalledBase> {
    public static final String BA_BASE_CNT_KEY="%s/base/cnt";
    public static final String BA_BASE_FMT_KEY="%s/base/%d";
    public static final String BA_BASE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/\\d+";
    public static final String BA_BASE_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/cnt";

    public static class LocalBucketDocument extends BucketDocument<BillingInstalledBase> {
        public LocalBucketDocument(BillingInstalledBase baInstBase){super(baInstBase);}
    }

    @Override
    public Class<? extends BucketDocument<BillingInstalledBase>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

    @Override
    public BillingInstalledBase buildKey(ICouchbaseSession session,BillingInstalledBase obj) throws DaoException,StorageException{
        long result = session.incrCounter(String.format(BA_BASE_CNT_KEY, obj.getBaLink().getKey()), 1);
        obj.getBaseMeta().setKey(String.format(BA_BASE_FMT_KEY, obj.getBaLink().getKey(), result));
        return obj;
    }

    @Override
    public String getKeyPattern(){
        return BA_BASE_KEY_PATTERN;
    }
}

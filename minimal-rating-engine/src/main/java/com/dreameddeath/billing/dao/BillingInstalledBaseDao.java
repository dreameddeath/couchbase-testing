package com.dreameddeath.billing.dao;


import com.dreameddeath.billing.model.installedbase.BillingInstalledBase;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBaseDao extends CouchbaseDocumentDao<BillingInstalledBase> {
    public static final String BA_BASE_CNT_KEY="%s/base/cnt";
    public static final String BA_BASE_FMT_KEY="%s/base/%d";
    public static final String BA_BASE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/\\d+";
    public static final String BA_BASE_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/cnt";


    private static GenericJacksonTranscoder<BillingInstalledBase> _tc = new GenericJacksonTranscoder<BillingInstalledBase>(BillingInstalledBase.class);

    public Transcoder<BillingInstalledBase> getTranscoder(){ return _tc; }

    public BillingInstalledBaseDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(BA_BASE_CNT_PATTERN).withDefaultValue(1L));
    }

    public void buildKey(BillingInstalledBase obj) throws DaoException{
        long result = obj.getSession().incrCounter(String.format(BA_BASE_CNT_KEY,obj.getBaLink().getKey()),1);
        obj.setKey(String.format(BA_BASE_FMT_KEY,obj.getBaLink().getKey(),result));
    }

    public String getKeyPattern(){
        return BA_BASE_KEY_PATTERN;
    }
}

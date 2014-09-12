package com.dreameddeath.installedbase.dao;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;

import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.installedbase.model.common.InstalledBase;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Created by ceaj8230 on 31/08/2014.
 */
public class InstalledBaseDao extends CouchbaseDocumentDao<InstalledBase> {
    public static final String INSTALLED_BASE_CNT_KEY="instBase/cnt";
    public static final String INSTALLED_BASE_FMT_KEY="instBase/%010d";
    public static final String INSTALLED_BASE_KEY_PATTERN="instBase/\\d{10}";
    public static final String INSTALLED_BASE_CNT_PATTERN="instBase/cnt";

    private static GenericJacksonTranscoder<InstalledBase> _tc = new GenericJacksonTranscoder<InstalledBase>(InstalledBase.class);

    @Override
    public Transcoder<InstalledBase> getTranscoder(){
        return _tc;
    }

    public InstalledBaseDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(INSTALLED_BASE_CNT_PATTERN).withDefaultValue(1L));
    }

    @Override
    public void buildKey(InstalledBase obj) throws DaoException{
        long result = obj.getSession().incrCounter(INSTALLED_BASE_CNT_KEY,1);
        obj.setDocumentKey(String.format(INSTALLED_BASE_FMT_KEY, result));
    }

    @Override
    public String getKeyPattern(){
        return INSTALLED_BASE_KEY_PATTERN;
    }

}

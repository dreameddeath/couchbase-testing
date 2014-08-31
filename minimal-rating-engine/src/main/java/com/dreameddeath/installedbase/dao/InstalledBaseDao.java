package com.dreameddeath.installedbase.dao;

import com.dreameddeath.core.dao.CouchbaseDocumentDao;

import com.dreameddeath.core.dao.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.installedbase.model.common.InstalledBase;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Created by ceaj8230 on 31/08/2014.
 */
public class InstalledBaseDao extends CouchbaseDocumentDao<InstalledBase> {
    public static final String INSTALLED_BASE_CNT_KEY="ba/cnt";
    public static final String INSTALLED_BASE_FMT_KEY="ba/%010d";
    public static final String INSTALLED_BASE_KEY_PATTERN="ba/\\d{10}";

    private static GenericJacksonTranscoder<InstalledBase> _tc = new GenericJacksonTranscoder<InstalledBase>(InstalledBase.class);

    @Override
    public Transcoder<InstalledBase> getTranscoder(){
        return _tc;
    }

    public InstalledBaseDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

    @Override
    public void buildKey(InstalledBase obj){
        long result = getClientWrapper().getClient().incr(INSTALLED_BASE_CNT_KEY,1,1,0);
        obj.setKey(String.format(INSTALLED_BASE_FMT_KEY,result));
    }

    @Override
    public String getKeyPattern(){
        return INSTALLED_BASE_KEY_PATTERN;
    }

}

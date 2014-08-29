package com.dreameddeath.billing.dao;


import com.dreameddeath.billing.model.installedbase.BillingInstalledBase;
import com.dreameddeath.core.dao.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBaseDao extends CouchbaseDocumentDao<BillingInstalledBase> {
    public static final String BA_BASE_CNT_KEY="%s/base/cnt";
    public static final String BA_BASE_FMT_KEY="%s/base/%d";
    public static final String BA_BASE_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/base/\\d+";


    private static GenericJacksonTranscoder<BillingInstalledBase> _tc = new GenericJacksonTranscoder<BillingInstalledBase>(BillingInstalledBase.class);

    public Transcoder<BillingInstalledBase> getTranscoder(){ return _tc; }

    public BillingInstalledBaseDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

    public void buildKey(BillingInstalledBase obj){
        long result = getClientWrapper().getClient().incr(String.format(BA_BASE_CNT_KEY,obj.getBaLink().getKey()),1,1,0);
        obj.setKey(String.format(BA_BASE_FMT_KEY,obj.getBaLink().getKey(),result));
    }

    public String getKeyPattern(){
        return BA_BASE_KEY_PATTERN;
    }
}

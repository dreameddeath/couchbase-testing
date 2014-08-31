package com.dreameddeath.rating.dao;


import com.dreameddeath.billing.dao.BillingAccountDao;
import com.dreameddeath.core.dao.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.rating.model.context.RatingContext;
import net.spy.memcached.transcoders.Transcoder;



public class RatingContextDao extends CouchbaseDocumentDao<RatingContext> {
    public static final String RATING_CTXT_CNT_KEY="%s/rat_ctxt/cnt";
    public static final String RATING_CTXT_FMT_KEY="%s/rat_ctxt/%d";
    public static final String RATING_CTXT_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/rat_ctxt/\\d+";
    
    private static GenericJacksonTranscoder<RatingContext> _tc = new GenericJacksonTranscoder<RatingContext>(RatingContext.class);
    
    public  Transcoder<RatingContext> getTranscoder(){
        return _tc;
    }
    
    public RatingContextDao(CouchbaseClientWrapper client, CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }
    
    public void buildKey(RatingContext obj){
        long result = getClientWrapper().getClient().incr(String.format(RATING_CTXT_CNT_KEY,obj.getBillingAccountLink().getKey()),1,1,0);
        obj.setKey(String.format(RATING_CTXT_FMT_KEY,obj.getBillingAccountLink().getKey(),result));
        //if(obj instanceof StandardRatingContext){
        //    getDaoFactory().getDaoFor(GenericCdrsBucket.class).buildKeysForLinks(((StandardRatingContext)obj).getCdrsBuckets());
        //}
    }
    
    public String getKeyPattern(){
        return RATING_CTXT_KEY_PATTERN;
    }
}
package com.dreameddeath.rating.dao.context;


import com.dreameddeath.rating.model.context.AbstractRatingContext;
import com.dreameddeath.billing.dao.BillingAccountDao;
import com.dreameddeath.common.dao.CouchbaseDocumentDao;
import com.dreameddeath.common.dao.CouchbaseDocumentDaoFactory;
import net.spy.memcached.transcoders.Transcoder;
import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;



public class AbstractRatingContextDao extends CouchbaseDocumentDao<AbstractRatingContext> {
    public static final String RATING_CTXT_CNT_KEY="%s/rat_ctxt/cnt";
    public static final String RATING_CTXT_FMT_KEY="%s/rat_ctxt/%d";
    public static final String RATING_CTXT_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/rat_ctxt/\\d+";
    
    private static GenericJacksonTranscoder<AbstractRatingContext> _tc = new GenericJacksonTranscoder<AbstractRatingContext>(AbstractRatingContext.class);
    
    public  Transcoder<AbstractRatingContext> getTranscoder(){
        return _tc;
    }
    
    public AbstractRatingContextDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }
    
    public void buildKey(AbstractRatingContext obj){
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
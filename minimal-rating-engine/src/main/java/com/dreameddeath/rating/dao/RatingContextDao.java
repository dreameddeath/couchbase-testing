package com.dreameddeath.rating.dao;


import com.dreameddeath.billing.dao.BillingAccountDao;
import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.model.common.BucketDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;
import com.dreameddeath.rating.model.context.RatingContext;


public class RatingContextDao extends CouchbaseDocumentDao<RatingContext> {
    public static final String RATING_CTXT_CNT_KEY="%s/rat_ctxt/cnt";
    public static final String RATING_CTXT_FMT_KEY="%s/rat_ctxt/%d";
    public static final String RATING_CTXT_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/rat_ctxt/\\d+";
    public static final String RATING_CTXT_CNT_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/rat_ctxt/cnt";

    public static class LocalBucketDocument extends BucketDocument<RatingContext> {
        public LocalBucketDocument(RatingContext obj){super(obj);}
    }

    private static GenericJacksonTranscoder<RatingContext> _tc = new GenericJacksonTranscoder<RatingContext>(RatingContext.class,LocalBucketDocument.class);
    
    public GenericTranscoder<RatingContext> getTranscoder(){
        return _tc;
    }
    
    public RatingContextDao(CouchbaseBucketWrapper client, BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(RATING_CTXT_CNT_KEY_PATTERN).withDefaultValue(1L));
    }


    @Override
    public void buildKey(RatingContext obj) throws DaoException{
        long result = obj.getBaseMeta().getSession().incrCounter(String.format(RATING_CTXT_CNT_KEY, obj.getBillingAccountLink().getKey()), 1);
        obj.getBaseMeta().setKey(String.format(RATING_CTXT_FMT_KEY, obj.getBillingAccountLink().getKey(), result));
        //if(obj instanceof StandardRatingContext){
        //    getDaoFactory().getDaoFor(GenericCdrsBucket.class).buildKeysForLinks(((StandardRatingContext)obj).getCdrsBuckets());
        //}
    }
    
    public String getKeyPattern(){
        return RATING_CTXT_KEY_PATTERN;
    }
}
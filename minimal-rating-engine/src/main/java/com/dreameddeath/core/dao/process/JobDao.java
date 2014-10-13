package com.dreameddeath.core.dao.process;


import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.model.common.BucketDocument;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class JobDao extends CouchbaseDocumentDaoWithUID<AbstractJob> {
    public static final String JOB_FMT_KEY="job/%s";
    public static final String JOB_KEY_PATTERN="job/.*";

    private static GenericJacksonTranscoder<AbstractJob> _tc = new GenericJacksonTranscoder<AbstractJob>(AbstractJob.class,LocalBucketDocument.class);

    public static class LocalBucketDocument extends BucketDocument<AbstractJob> {
        public LocalBucketDocument(AbstractJob obj){super(obj);}
    }

    @Override
    public GenericTranscoder<AbstractJob> getTranscoder(){
        return _tc;
    }

    public JobDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

    @Override
    public void buildKey(AbstractJob obj){
        obj.getMeta().setKey(String.format(JOB_FMT_KEY, obj.getUid().toString()));
    }

    @Override
    public String getKeyPattern(){
        return JOB_KEY_PATTERN;
    }

    @Override
    public String getKeyFromUID(String uid){return String.format(JOB_FMT_KEY,uid);}
}

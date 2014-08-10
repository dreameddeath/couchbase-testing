package com.dreameddeath.core.dao;


import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class JobDao extends CouchbaseDocumentDaoWithUID<AbstractJob> {
    public static final String JOB_FMT_KEY="job/%s";
    public static final String JOB_KEY_PATTERN="job/.*";

    private static GenericJacksonTranscoder<AbstractJob> _tc = new GenericJacksonTranscoder<AbstractJob>(AbstractJob.class);

    @Override
    public Transcoder<AbstractJob> getTranscoder(){
        return _tc;
    }

    public JobDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

    @Override
    public void buildKey(AbstractJob obj){
        obj.setKey(String.format(JOB_FMT_KEY,obj.getUid().toString()));
    }

    @Override
    public String getKeyPattern(){
        return JOB_KEY_PATTERN;
    }

    @Override
    public String getKeyFromUID(String uid){return String.format(JOB_FMT_KEY,uid);}
}

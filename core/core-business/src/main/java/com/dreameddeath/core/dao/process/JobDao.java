package com.dreameddeath.core.dao.process;


import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class JobDao extends CouchbaseDocumentDaoWithUID<AbstractJob> {
    public static final String JOB_FMT_KEY="job/%s";
    public static final String JOB_KEY_PATTERN="job/[^/]*";

    public static class LocalBucketDocument extends BucketDocument<AbstractJob> {
        public LocalBucketDocument(AbstractJob obj){super(obj);}
    }


    @Override
    public Class<? extends BucketDocument<AbstractJob>> getBucketDocumentClass() { return LocalBucketDocument.class; }

    @Override
    public AbstractJob buildKey(ICouchbaseSession session,AbstractJob obj){
        obj.getMeta().setKey(String.format(JOB_FMT_KEY, obj.getUid().toString()));
        return obj;
    }

    @Override
    public String getKeyPattern(){
        return JOB_KEY_PATTERN;
    }

    @Override
    public String getKeyFromUID(String uid){return String.format(JOB_FMT_KEY,uid);}
}

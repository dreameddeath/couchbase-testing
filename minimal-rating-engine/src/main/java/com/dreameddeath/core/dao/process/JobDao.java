/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.dao.process;


import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.GenericJacksonTranscoder;
import com.dreameddeath.core.couchbase.GenericTranscoder;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.model.document.BucketDocument;
import com.dreameddeath.core.process.common.AbstractJob;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class JobDao extends CouchbaseDocumentDaoWithUID<AbstractJob> {
    public static final String JOB_FMT_KEY="job/%s";
    public static final String JOB_KEY_PATTERN="job/.*";

    private static GenericJacksonTranscoder<AbstractJob> tc = new GenericJacksonTranscoder<AbstractJob>(AbstractJob.class,LocalBucketDocument.class);

    public static class LocalBucketDocument extends BucketDocument<AbstractJob> {
        public LocalBucketDocument(AbstractJob obj){super(obj);}
    }

    @Override
    public GenericTranscoder<AbstractJob> getTranscoder(){
        return tc;
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

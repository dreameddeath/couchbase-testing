/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao.process;


import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DaoForClass(AbstractJob.class)
public class JobDao extends BusinessCouchbaseDocumentDaoWithUID<AbstractJob> {
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

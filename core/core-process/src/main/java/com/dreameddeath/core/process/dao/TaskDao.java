/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.process.dao;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import io.reactivex.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 22/12/2015.
 */

@DaoForClass(AbstractTask.class)
public class TaskDao extends CouchbaseDocumentWithKeyPatternDao<AbstractTask> {
    public static final String FMT_KEY=JobDao.JOB_FMT_KEY+"/task/%s";
    public static final String PATTERN_KEY=JobDao.JOB_KEY_PATTERN+"/task/{tid:\\d+(?:-\\d+)*}";
    public static final String TASK_CNT_FMT=JobDao.JOB_FMT_KEY+"/taskcnt";
    public static final String TASK_CNT_PATTERN=JobDao.JOB_KEY_PATTERN+"/taskcnt";


    @BucketDocumentForClass(AbstractTask.class)
    public static class LocalBucketDocument extends BucketDocument<AbstractTask> {
        public LocalBucketDocument(AbstractTask obj){super(obj);}
    }

    @Override
    public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
        List<CouchbaseCounterDao.Builder> result = new ArrayList<>();
        result.add(
                new CouchbaseCounterDao.Builder().withKeyPattern(TASK_CNT_PATTERN).withBaseDao(this)
        );
        return result;
    }


    @Override
    public boolean isKeySharedAcrossDomains() {
        return true;
    }

    @Override
    protected String getKeyRawPattern() {
        return PATTERN_KEY;
    }

    @Override
    protected AbstractTask updateTransientFromKeyPattern(AbstractTask obj, String... params) {
        obj.setJobUid(UUID.fromString(params[0]));
        String parentId = null;
        String id = params[1];
        if((id!=null) && (id.contains("-"))){
            parentId = id.substring(0,id.lastIndexOf("-"));
            id = id.substring(id.lastIndexOf("-"));
        }
        obj.setParentTaskId(parentId);
        obj.setId(id);
        return obj;
    }

    @Override
    public String getKeyFromParams(Object... params) {
        return String.format(FMT_KEY,
                params[0],
                params[1]
        );
    }

    @Override
    public Single<AbstractTask> asyncBuildKey(ICouchbaseSession session, final AbstractTask newObject) throws DaoException {
        newObject.getBaseMeta().setKey(getKeyFromParams(newObject.getJobUid(),newObject.getId()));
        return Single.just(newObject);
    }

    @Override
    public Class<? extends BucketDocument<AbstractTask>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

}

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

package com.dreameddeath.core.process.dao;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.java.utils.NumberUtils;
import com.dreameddeath.core.process.model.AbstractTask;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 22/12/2015.
 */

@DaoForClass(AbstractTask.class)
public class TaskDao extends CouchbaseDocumentWithKeyPatternDao<AbstractTask> {
    public static final String FMT_KEY=JobDao.JOB_FMT_KEY+"/task/%d";
    public static final String PATTERN_KEY=JobDao.JOB_KEY_PATTERN+"/task/{tid:\\d+}";
    public static final String TASK_CNT_FMT=JobDao.JOB_FMT_KEY+"/task/cnt";
    public static final String TASK_CNT_PATTERN=JobDao.JOB_KEY_PATTERN+"/task/cnt";


    @BucketDocumentForClass(AbstractTask.class)
    public static class LocalBucketDocument extends BucketDocument<AbstractTask> {
        public LocalBucketDocument(AbstractTask obj){super(obj);}
    }

    @Override
    public List<CouchbaseCounterDao.Builder> getCountersBuilder() {
        List<CouchbaseCounterDao.Builder> result = new ArrayList<>();
        result.add(
                new CouchbaseCounterDao.Builder().withKeyPattern(TASK_CNT_PATTERN).withDefaultValue(1).withBaseDao(this)
        );
        return result;
    }


    @Override
    protected String getKeyRawPattern() {
        return PATTERN_KEY;
    }

    @Override
    protected AbstractTask updateTransientFromKeyPattern(AbstractTask obj, String... params) {
        obj.setJobUid(UUID.fromString(params[0]));
        obj.setId(Integer.valueOf(params[1]));
        return obj;
    }

    @Override
    public String getKeyFromParams(Object... params) {
        return String.format(FMT_KEY,
                params[0],
                NumberUtils.asInt(params[1])
        );
    }

    @Override
    public Observable<AbstractTask> asyncBuildKey(ICouchbaseSession session, final AbstractTask newObject) throws DaoException {
        return session.asyncIncrCounter(String.format(TASK_CNT_FMT,newObject.getJobUid()),1)
                .map(new BuildKeyFromCounterFunc(newObject,newObject.getJobUid().toString()));
    }

    @Override
    public Class<? extends BucketDocument<AbstractTask>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

    @Override
    public Class<AbstractTask> getBaseClass() {
        return AbstractTask.class;
    }
}

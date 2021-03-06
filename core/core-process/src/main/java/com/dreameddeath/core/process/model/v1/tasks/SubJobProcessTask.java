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

package com.dreameddeath.core.process.model.v1.tasks;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import io.reactivex.Single;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DocumentEntity
public abstract class SubJobProcessTask<T extends AbstractJob> extends AbstractTask {
    @DocumentProperty("subJobId")
    private Property<UUID> subJobId =new StandardProperty<>(SubJobProcessTask.this);

    public UUID getSubJobId(){ return subJobId.get(); }
    public void setSubJobId(UUID subJobId){this.subJobId.set(subJobId);}

    public T blockingGetJob(ICouchbaseSession session) throws DaoException,StorageException{return (T)session.toBlocking().blockingGetFromUID(getSubJobId().toString(),AbstractJob.class);}
    public Single<T> getJob(ICouchbaseSession session) {return (Single<T>)session.asyncGetFromUID(getSubJobId().toString(),AbstractJob.class);}
}

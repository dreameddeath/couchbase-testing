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

package com.dreameddeath.core.business.dao;

import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class BusinessCouchbaseDocumentDao<T extends BusinessDocument> extends CouchbaseDocumentDao<T> {
    private final static Logger LOG= LoggerFactory.getLogger(BusinessCouchbaseDocumentDao.class);

    @Override
    public BlockingDao toBlocking(){
        return new BlockingDao();
    }

    protected void updateRevision(ICouchbaseSession session,T obj){
        obj.incDocRevision(session);
        obj.updateDocLastModDate(session);
    }

    @Override
    public Single<T> asyncUpdate(ICouchbaseSession session, T obj, boolean isCalcOnly){
        updateRevision(session,obj);
        Set<String> keysToRemove=obj.getMeta().getRemovedUniqueKeys();
        Single<T> result = super.asyncUpdate(session,obj, isCalcOnly);
        result.doOnSuccess(new CleanKeysAction(session,keysToRemove));
        return result;
    }

    @Override
    public Single<T> asyncDelete(ICouchbaseSession session, final T doc, boolean isCalcOnly){
        Set<String> keysToRemove=doc.getMeta().getRemovedUniqueKeys();
        Single<T> result = super.asyncDelete(session,doc,isCalcOnly);
        result.doOnSuccess(new CleanKeysAction(session,keysToRemove));
        return result;
    }

    public class CleanKeysAction implements Consumer<T> {
        private final Set<String> keysToRemove;
        private final ICouchbaseSession session;
        public CleanKeysAction(ICouchbaseSession session,Set<String> keys){
            keysToRemove =keys;
            this.session =session;
        }
        @Override
        public void accept(T t) {
            for(String key :keysToRemove){
                try {
                    session.toBlocking().blockingRemoveUniqueKey(key);
                }
                catch(Throwable e){
                    LOG.error("Cannot delete unique key",key);
                }
            }
        }
    }

    public class BlockingDao extends CouchbaseDocumentDao<T>.BlockingDao{

    }
}
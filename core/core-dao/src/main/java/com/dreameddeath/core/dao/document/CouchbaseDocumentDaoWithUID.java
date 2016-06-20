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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 17/05/2015.
 */
public abstract class CouchbaseDocumentDaoWithUID<T extends CouchbaseDocument> extends CouchbaseDocumentWithKeyPatternDao<T> implements IDaoForDocumentWithUID<T> {
    public final String getKeyFromUID(String uid){
        return getKeyFromParams(uid);
    }

    @Override
    public BlockingDao toBlocking(){
        return new BlockingDao();
    }

    @Override
    public Observable<T> asyncGetFromUid(ICouchbaseSession session, String uid){
        return asyncGet(session,getKeyFromUID(uid));
    }

    public class BlockingDao extends CouchbaseDocumentWithKeyPatternDao<T>.BlockingDao implements IBlockingDaoForDocumentWithUID<T>{
        public T getFromUID(ICouchbaseSession session,String uid) throws DaoException,StorageException {
            return get(session, getKeyFromUID(uid));
        }
    }
}

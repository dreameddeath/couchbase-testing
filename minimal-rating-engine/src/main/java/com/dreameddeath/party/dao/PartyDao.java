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

package com.dreameddeath.party.dao;

import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.GenericJacksonTranscoder;
import com.dreameddeath.core.couchbase.GenericTranscoder;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.model.document.BucketDocument;
import com.dreameddeath.party.model.base.Party;

public class PartyDao extends CouchbaseDocumentDaoWithUID<Party> {
    public static final String PARTY_CNT_KEY="party/cnt";
    public static final String PARTY_FMT_KEY="party/%010d";
    public static final String PARTY_FMT_UID="%010d";
    public static final String PARTY_KEY_PATTERN="party/\\d{10}";
    public static final String PARTY_CNT_KEY_PATTERN="party/cnt";

    public static class LocalBucketDocument extends BucketDocument<Party>{
        public LocalBucketDocument(Party party){super(party);}
    }

    private static GenericJacksonTranscoder<Party> _tc = new GenericJacksonTranscoder<Party>(Party.class,LocalBucketDocument.class);

    public GenericTranscoder<Party> getTranscoder(){
        return _tc;
    }

    public PartyDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(PARTY_CNT_KEY_PATTERN).withDefaultValue(1L));
    }

    public void buildKey(Party obj) throws DaoException {
        long result = obj.getBaseMeta().getSession().incrCounter(PARTY_CNT_KEY, 1);
        obj.getBaseMeta().setKey(String.format(PARTY_FMT_KEY, result));
        if(obj.getUid()==null){
            obj.setUid(String.format(PARTY_FMT_UID,result));
        }
    }

    public String getKeyPattern(){
        return PARTY_KEY_PATTERN;
    }

    public String getKeyFromUID(String uid){return String.format(PARTY_FMT_KEY,Long.parseLong(uid));}
}
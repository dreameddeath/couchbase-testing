package com.dreameddeath.party.dao;

import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.party.model.base.Party;

import java.util.Arrays;
import java.util.List;

@DaoForClass(Party.class)
public class PartyDao extends CouchbaseDocumentDaoWithUID<Party> {
    public static final String PARTY_CNT_KEY="party/cnt";
    public static final String PARTY_FMT_KEY="party/%010d";
    public static final String PARTY_FMT_UID="%010d";
    public static final String PARTY_KEY_PATTERN="party/\\d{10}";
    public static final String PARTY_CNT_KEY_PATTERN="party/cnt";

    public static class LocalBucketDocument extends BucketDocument<Party>{
        public LocalBucketDocument(Party party){super(party);}
    }


    @Override
    public Class<? extends BucketDocument<Party>> getBucketDocumentClass() {
        return LocalBucketDocument.class;
    }

    @Override
    public List<CouchbaseCounterDao.Builder> getCountersBuilder(){
        return Arrays.asList(
                new CouchbaseCounterDao.Builder().withKeyPattern(PARTY_CNT_KEY_PATTERN).withDefaultValue(1L).withBaseDao(this)
        );
    }

    @Override
    public List<CouchbaseUniqueKeyDao.Builder> getUniqueKeysBuilder(){
        return Arrays.asList(
                new CouchbaseUniqueKeyDao.Builder().withNameSpace("personName").withBaseDao(this)
        );
    }

    @Override
    public Party buildKey(ICouchbaseSession session,Party obj) throws DaoException,StorageException {
        long result = session.incrCounter(PARTY_CNT_KEY, 1);
        obj.getBaseMeta().setKey(String.format(PARTY_FMT_KEY, result));
        if(obj.getUid()==null){
            obj.setUid(String.format(PARTY_FMT_UID,result));
        }
        return obj;
    }

    @Override
    public String getKeyPattern(){
        return PARTY_KEY_PATTERN;
    }

    @Override
    public String getKeyFromUID(String uid){return String.format(PARTY_FMT_KEY,Long.parseLong(uid));}
}
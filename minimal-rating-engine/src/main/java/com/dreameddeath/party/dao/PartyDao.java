package com.dreameddeath.party.dao;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

public class PartyDao extends CouchbaseDocumentDaoWithUID<Party> {
    public static final String PARTY_CNT_KEY="party/cnt";
    public static final String PARTY_FMT_KEY="party/%010d";
    public static final String PARTY_FMT_UID="%010d";
    public static final String PARTY_KEY_PATTERN="party/\\d{10}";
    public static final String PARTY_CNT_KEY_PATTERN="party/cnt";

    private static GenericJacksonTranscoder<Party> _tc = new GenericJacksonTranscoder<Party>(Party.class);

    public  Transcoder<Party> getTranscoder(){
        return _tc;
    }

    public PartyDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(PARTY_CNT_KEY_PATTERN).withDefaultValue(1L));
    }

    public void buildKey(Party obj) throws DaoException {
        long result = obj.getSession().incrCounter(PARTY_CNT_KEY,1);
        obj.setKey(String.format(PARTY_FMT_KEY,result));
        if(obj.getUid()==null){
            obj.setUid(String.format(PARTY_FMT_UID,result));
        }
    }

    public String getKeyPattern(){
        return PARTY_KEY_PATTERN;
    }

    public String getKeyFromUID(String uid){return String.format(PARTY_FMT_KEY,Long.parseLong(uid));}
}
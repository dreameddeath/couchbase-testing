package com.dreameddeath.party.dao;

import com.dreameddeath.common.dao.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.party.model.Party;
import com.dreameddeath.common.dao.CouchbaseDocumentDao;
import com.dreameddeath.common.dao.CouchbaseDocumentDaoFactory;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;
import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

public class PartyDao extends CouchbaseDocumentDaoWithUID<Party> {
    public static final String PARTY_CNT_KEY="party/cnt";
    public static final String PARTY_FMT_KEY="party/%010d";
    public static final String PARTY_FMT_UID="%010d";
    public static final String PARTY_KEY_PATTERN="party/\\d{10}";

    private static GenericJacksonTranscoder<Party> _tc = new GenericJacksonTranscoder<Party>(Party.class);

    public  Transcoder<Party> getTranscoder(){
        return _tc;
    }

    public PartyDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

    public void buildKey(Party obj){
        long result = getClientWrapper().getClient().incr(PARTY_CNT_KEY,1,1,0);
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
package com.dreameddeath.core.dao;

import com.dreameddeath.core.model.document.CouchbaseDocumentUniqueKey;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import java.security.NoSuchAlgorithmException;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class UniqueKeyDao extends CouchbaseDocumentDao<CouchbaseDocumentUniqueKey> {
    public static final String UNIQ_FMT_KEY="uniq/%s";
    public static final String UNIQ_KEY_PATTERN="uniq/.*";

    private static GenericJacksonTranscoder<CouchbaseDocumentUniqueKey> _tc = new GenericJacksonTranscoder<CouchbaseDocumentUniqueKey>(CouchbaseDocumentUniqueKey.class);
    @Override
    public Transcoder<CouchbaseDocumentUniqueKey> getTranscoder(){
        return _tc;
    }

    public UniqueKeyDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

    @Override
    public void buildKey(CouchbaseDocumentUniqueKey obj){
        //TODO throw Error
        try {
            obj.setKey(String.format(UNIQ_FMT_KEY, obj.getHashKey()));
        }
        catch(NoSuchAlgorithmException e){

        }
    }

    public String getKeyPattern(){
        return "uniq/.*$";
    }

}

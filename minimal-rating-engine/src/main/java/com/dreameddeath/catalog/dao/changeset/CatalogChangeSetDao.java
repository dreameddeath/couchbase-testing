package com.dreameddeath.catalog.dao.changeset;

import com.dreameddeath.catalog.dao.CatalogElementDao;
import com.dreameddeath.catalog.model.changeset.CatalogChangeSet;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public class CatalogChangeSetDao extends CatalogElementDao<CatalogChangeSet> {
    public static final String CHANGE_SET_DOMAIN="changeset";
    private static GenericJacksonTranscoder<CatalogChangeSet> _tc = new GenericJacksonTranscoder<CatalogChangeSet>(CatalogChangeSet.class);

    @Override
    public Transcoder<CatalogChangeSet> getTranscoder(){return _tc;}

    @Override
    public String getKeyDomain(){ return CHANGE_SET_DOMAIN;}

    public CatalogChangeSetDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

}

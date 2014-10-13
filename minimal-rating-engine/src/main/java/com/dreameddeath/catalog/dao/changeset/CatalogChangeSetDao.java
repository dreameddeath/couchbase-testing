package com.dreameddeath.catalog.dao.changeset;

import com.dreameddeath.catalog.dao.CatalogElementDao;
import com.dreameddeath.catalog.model.changeset.CatalogChangeSet;
import com.dreameddeath.core.dao.common.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.model.common.BucketDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public class CatalogChangeSetDao extends CatalogElementDao<CatalogChangeSet> {
    public static final String CHANGE_SET_DOMAIN="changeset";

    public static class LocalBucketDocument extends BucketDocument<CatalogChangeSet> {
        public LocalBucketDocument(CatalogChangeSet obj){super(obj);}
    }

    private static GenericJacksonTranscoder<CatalogChangeSet> _tc = new GenericJacksonTranscoder<CatalogChangeSet>(CatalogChangeSet.class,LocalBucketDocument.class);

    @Override
    public GenericTranscoder<CatalogChangeSet> getTranscoder(){return _tc;}

    @Override
    public String getKeyDomain(){ return CHANGE_SET_DOMAIN;}

    public CatalogChangeSetDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }

}

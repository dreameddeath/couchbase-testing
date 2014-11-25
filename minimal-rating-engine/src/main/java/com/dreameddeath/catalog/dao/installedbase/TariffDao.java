package com.dreameddeath.catalog.dao.installedbase;

import com.dreameddeath.catalog.dao.CatalogElementDao;
import com.dreameddeath.catalog.model.installedbase.Tariff;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.model.document.BucketDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public class TariffDao extends CatalogElementDao<Tariff> {
    public static final String TARIFF_DOMAIN="tariff";

    public static class LocalBucketDocument extends BucketDocument<Tariff> {
        public LocalBucketDocument(Tariff obj){super(obj);}
    }

    private static GenericJacksonTranscoder<Tariff> _tc = new GenericJacksonTranscoder<Tariff>(Tariff.class,LocalBucketDocument.class);



    @Override
    public GenericTranscoder<Tariff> getTranscoder(){return _tc;}

    @Override
    public String getKeyDomain(){ return TARIFF_DOMAIN;}

    public TariffDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }
}

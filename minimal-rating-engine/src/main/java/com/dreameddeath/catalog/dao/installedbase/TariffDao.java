package com.dreameddeath.catalog.dao.installedbase;

import com.dreameddeath.catalog.dao.CatalogElementDao;
import com.dreameddeath.catalog.model.installedbase.Tariff;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public class TariffDao extends CatalogElementDao<Tariff> {
    public static final String TARIFF_DOMAIN="tariff";
    private static GenericJacksonTranscoder<Tariff> _tc = new GenericJacksonTranscoder<Tariff>(Tariff.class);

    @Override
    public Transcoder<Tariff> getTranscoder(){return _tc;}

    @Override
    public String getKeyDomain(){ return TARIFF_DOMAIN;}

    public TariffDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
        super(client,factory);
    }
}

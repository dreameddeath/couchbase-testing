/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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

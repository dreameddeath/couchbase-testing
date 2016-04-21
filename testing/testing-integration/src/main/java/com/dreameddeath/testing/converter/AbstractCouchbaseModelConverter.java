package com.dreameddeath.testing.converter;

import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentObjectMapperConfigurator;
import com.dreameddeath.testing.dataset.converter.AbstractObjectMapperBasedConverter;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public abstract class AbstractCouchbaseModelConverter<T> extends AbstractObjectMapperBasedConverter<T> {

    public AbstractCouchbaseModelConverter(){
        super(ObjectMapperFactory.BASE_INSTANCE.getMapper(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC));
    }

}

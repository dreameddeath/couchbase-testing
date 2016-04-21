package com.dreameddeath.testing.converter;

import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class CouchbaseDocumentElementConverter extends AbstractCouchbaseModelConverter<CouchbaseDocumentElement>{
    @Override
    public boolean canMap(Class<?> clazz) {
        return CouchbaseDocumentElement.class.isAssignableFrom(clazz);
    }
}

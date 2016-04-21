package com.dreameddeath.testing.converter;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class CouchbaseDocumentConverter extends AbstractCouchbaseModelConverter<CouchbaseDocument> {
    @Override
    public boolean canMap(Class<?> clazz) {
        return CouchbaseDocument.class.isAssignableFrom(clazz);
    }
}

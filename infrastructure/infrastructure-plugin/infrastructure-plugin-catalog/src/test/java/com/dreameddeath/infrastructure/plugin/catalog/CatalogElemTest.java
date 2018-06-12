package com.dreameddeath.infrastructure.plugin.catalog;

import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;

@DaoEntity(baseDao = CouchbaseDocumentWithKeyPatternDao.class,dbPath = "test/",idFormat = "%05d",idPattern = "\\d{5}")
@Counter(name="cnt",dbName = "cnt",isKeyGen = true)
@DocumentEntity(domain = "test",version = "1.0.0")
public class CatalogElemTest extends CatalogElement {
    @DocumentProperty
    public String value1;
}

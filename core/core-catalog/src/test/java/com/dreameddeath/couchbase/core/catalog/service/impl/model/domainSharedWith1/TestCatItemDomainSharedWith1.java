package com.dreameddeath.couchbase.core.catalog.service.impl.model.domainSharedWith1;

import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;

@DaoEntity(baseDao = CouchbaseDocumentWithKeyPatternDao.class,dbPath = "cat/shared1",idFormat = "%010d",idPattern = "\\d{10}")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
@DocumentEntity
public class TestCatItemDomainSharedWith1 extends CatalogElement {
    @DocumentProperty
    public String value1;
}

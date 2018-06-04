package com.dreameddeath.couchbase.core.catalog.service.impl.model.domain2;

import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;

@DaoEntity(baseDao = CouchbaseDocumentWithKeyPatternDao.class,dbPath = "cat/item2/",idFormat = "%010d",idPattern = "\\d{10}")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
@DocumentEntity
public class TestCatItemDomain2 extends CatalogElement {
}

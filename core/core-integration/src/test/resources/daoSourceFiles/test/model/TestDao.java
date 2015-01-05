package test.model;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;

@DocumentDef(domain="test",name="daoProccessor",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDao.class,dbPath = "test",idFormat = "%010d",idPattern = "\\d{10}")
public class TestDao extends BusinessCouchbaseDocument {
    @DocumentProperty("value")
    public String value;
}
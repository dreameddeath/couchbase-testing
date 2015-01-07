package test.model;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.dao.DaoEntity;
import com.dreameddeath.core.annotation.dao.ParentEntity;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;

@DocumentDef(domain="test",name="daoProccessorChild",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDao.class,dbPath = "child",idFormat = "%010d",idPattern = "\\d{10}")
@ParentEntity(c=TestDao.class,keyPath = "parent.key",separator = "/")
public class TestDaoChild extends BusinessCouchbaseDocument {
    @DocumentProperty("value")
    public String value;
    @DocumentProperty("parent")
    public TestDaoLink parent;
}
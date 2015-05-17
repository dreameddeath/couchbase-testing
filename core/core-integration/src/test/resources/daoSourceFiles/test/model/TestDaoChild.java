package test.model;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.dao.helper.annotation.DaoEntity;
import com.dreameddeath.core.dao.helper.annotation.Counter;
import com.dreameddeath.core.dao.helper.annotation.ParentEntity;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.business.model.BusinessDocument;

@DocumentDef(domain="test",name="daoProccessorChild",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDao.class,dbPath = "child/",idFormat = "%010d",idPattern = "\\d{10}")
@ParentEntity(c=TestDao.class,keyPath = "parent.key",separator = "/")
@Counter(name="cnt",dbName = "cnt",isKeyGen = true)
public class TestDaoChild extends BusinessDocument {
    @DocumentProperty("value")
    public String value;
    @DocumentProperty("parent")
    public TestDaoLink parent;
}
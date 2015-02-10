package test.model;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.dao.*;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.model.view.impl.ViewStringKeyTranscoder;
import com.dreameddeath.core.model.view.impl.ViewStringTranscoder;
import com.dreameddeath.core.model.view.impl.ViewStringKeyTranscoder;

@DocumentDef(domain="test",name="daoProccessor",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDao.class,dbPath = "test/",idFormat = "%010d",idPattern = "\\d{10}")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
@Counter(name= "checks",dbName = "checks")
@View(name = "listView",
        contentFilename = "viewContent.cbView",
        keyDef = @ViewKeyDef(type=String.class,transcoder = ViewStringKeyTranscoder.class),
        valueDef = @ViewValueDef(type=String.class,transcoder = ViewStringTranscoder.class))
@View(name = "valueView",
        content = "emit(doc.value,doc.value);",
        keyDef = @ViewKeyDef(type=String.class,transcoder = ViewStringKeyTranscoder.class),
        valueDef = @ViewValueDef(type=String.class,transcoder = ViewStringTranscoder.class))
public class TestDao extends BusinessCouchbaseDocument {
    @DocumentProperty("value")
    public String value;
}
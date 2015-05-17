package test.model;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.dao.helper.annotation.*;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.dao.model.view.impl.ViewStringKeyTranscoder;
import com.dreameddeath.core.dao.model.view.impl.ViewStringTranscoder;

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
public class TestDao extends BusinessDocument {
    @DocumentProperty("value")
    public String value;
}
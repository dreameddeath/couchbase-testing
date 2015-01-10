package test.model;

import java.util.Collection;
import java.util.List;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;

import com.dreameddeath.core.annotation.dao.Counter;
import com.dreameddeath.core.annotation.dao.DaoEntity;
import com.dreameddeath.core.annotation.dao.UidDef;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;

import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

@DocumentDef(domain="test",name="daoProccessorUid",version = "1.0.0")
@DaoEntity(baseDao = BusinessCouchbaseDocumentDaoWithUID.class,dbPath = "testUid/",idFormat = "%010d",idPattern = "\\d{10}")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
@Counter(name= "checks",dbName = "checks")
@UidDef(fieldName = "uid")
public class TestDaoUid extends BusinessCouchbaseDocument {
    @DocumentProperty("uid")
    public String uid;
    @DocumentProperty("value")
    public String value;
    /**
     *  testList : List of strings
     */
    @DocumentProperty("testList")
    private ListProperty<String> _testList = new ArrayListProperty<String>(TestDaoUid.this);

    // TestList Accessors
    public List<String> getTestList() { return _testList.get(); }
    public void setTestList(Collection<String> vals) { _testList.set(vals); }
    public boolean addTestList(String val){ return _testList.add(val); }
    public boolean removeTestList(String val){ return _testList.remove(val); }
}
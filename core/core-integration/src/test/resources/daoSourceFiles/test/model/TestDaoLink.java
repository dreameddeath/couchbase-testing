package test.model;

import com.dreameddeath.core.model.business.BusinessCouchbaseDocumentLink;

public class TestDaoLink extends BusinessCouchbaseDocumentLink<TestDao> {
    public TestDaoLink(){}
    public TestDaoLink (TestDao src){super(src);}
    public TestDaoLink(TestDaoLink srcLink){super(srcLink);}
}
package test.model;

import com.dreameddeath.core.business.model.BusinessDocumentLink;

public class TestDaoLink extends BusinessDocumentLink<TestDao> {
    public TestDaoLink(){}
    public TestDaoLink (TestDao src){super(src);}
    public TestDaoLink(TestDaoLink srcLink){super(srcLink);}
}
package com.dreameddeath.billing.model.order;


import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.test.Utils;
import com.dreameddeath.party.dao.PartyDao;
import com.dreameddeath.party.model.base.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BillingOrderTest extends Assert {

    Utils.TestEnvironment _env;
    @Before
    public void initTest() throws  Exception{
        _env = new Utils.TestEnvironment("billingOrder");
        _env.addDocumentDao(new PartyDao());
        _env.addDocumentDao((CouchbaseDocumentDao) BillingOrderTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.account.BillingAccountDao").newInstance());
        _env.addDocumentDao((CouchbaseDocumentDao) BillingOrderTest.class.getClassLoader().loadClass("com.dreameddeath.billing.dao.order.BillingOrderDao").newInstance());
        // _env.addDocumentDao(new TestDaoProcesorDao(),TestDaoProcessor.class);
        _env.start();
    }


    @Test
    public void test()throws Throwable{
        ICouchbaseSession session=_env.getSessionFactory().newReadWriteSession(null);
        Person person = session.newEntity(Person.class);
        person.setFirstName("test");
        person.setLastName("test");
        session.save(person);
        BillingAccount ba = new BillingAccount();
        ba.setBillDay(1);
        ba.setBillCycleLength(1);
        session.save(ba);
        BillingOrder bo = new BillingOrder();
        bo.setBillingAccount(ba.newLink());
        session.save(bo);

        session.reset();
        BillingOrder result = session.get(bo.getMeta().getKey(),BillingOrder.class);
        assertEquals(result.getClass(),BillingOrder.class);
        assertEquals("ba/0000000001/order/00001",result.getMeta().getKey());
    }
}
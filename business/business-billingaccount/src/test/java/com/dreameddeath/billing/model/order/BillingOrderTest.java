/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.billing.model.order;


import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.party.dao.PartyDao;
import com.dreameddeath.party.model.base.Person;
import com.dreameddeath.testing.Utils;
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
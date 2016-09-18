/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.helper;

import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.helper.service.DaoHelperServiceUtils;
import com.dreameddeath.core.helper.service.SerializableViewQueryRow;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.client.rest.rxjava.RxJavaWebTarget;
import com.dreameddeath.core.service.testing.TestingRestServer;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentObjectMapperConfigurator;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentProviderInterceptor;
import com.dreameddeath.testing.Utils;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Christophe Jeunesse on 18/12/2014.
 */
public class ViewTest {

    private Utils.TestEnvironment env;
    private TestingRestServer server;
    private CuratorTestUtils testUtils;

    @Before
    public void initTest() throws  Exception{
        env = new Utils.TestEnvironment("ViewTests", Utils.TestEnvironment.TestEnvType.COUCHBASE);
        env.addDocumentDao(new TestDao(), TestDoc.class);
        env.addDocumentDao(new TestChildDao(),TestDocChild.class);
        env.start();

        testUtils = new CuratorTestUtils().prepare(1);
        server = new TestingRestServer("serverTesting", testUtils.getClient("serverTesting"), ObjectMapperFactory.BASE_INSTANCE.getMapper(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC));
        server.registerBeanObject("couchbaseSessionFactory",env.getSessionFactory());
        //server.registerBeanObject("userFactory",new StandardMockUserFactory());

        server.registerBeanClass("TestDaoRestService",TestDaoRestService.class);
        server.registerBeanClass("TestChildDaoRestService",TestChildDaoRestService.class);
        /*TestDaoRestService service = new TestDaoRestService();
        service.setSessionFactory(env.getSessionFactory());
        service.setUserFactory(new StandardMockUserFactory());
        server.registerService("TestDaoRestService", service);*/

        /*TestChildDaoRestService childService = new TestChildDaoRestService();
        childService.setSessionFactory(env.getSessionFactory());
        childService.setUserFactory(new StandardMockUserFactory());
        server.registerService("TestChildDaoRestService", childService);*/

        server.start();
    }

    @Test
    public void testView() throws Exception{
        ICouchbaseSession session = env.getSessionFactory().newReadWriteSession(null);
        for(int i=0;i<10;++i){
            TestDoc doc = session.newEntity(TestDoc.class);
            doc.strVal="test "+i;
            doc.doubleVal=i*1.1;
            doc.longVal=i+1L;
            doc.intVal=i;
            doc.boolVal= (i % 2 == 0);
            doc.arrayVal = new ArrayList<>(i);
            for(int j=0;j<i;++j){
                TestDoc.TestDocSubElem elem=new TestDoc.TestDocSubElem();
                elem.longVal=j+1L;
                doc.arrayVal.add(elem);
            }
            session.toBlocking().blockingSave(doc);
            for(int j=0;j<i;++j) {
                TestDocChild child = session.newEntity(TestDocChild.class);
                child.parent= new TestDocLink(doc);
                child.value = String.format("Child:%d",j);
                session.toBlocking().blockingSave(child);
            }
        }

        IViewQuery<String,String,TestDoc> query = session.initViewQuery(TestDoc.class, "testView");
        query.withStartKey("test 3").withEndKey("test 6",false).withLimit(20).syncWithDoc();
        IViewQueryResult<String,String,TestDoc> result = session.executeQuery(query);
        List<IViewQueryRow<String,String,TestDoc>> rows = result.getAllRows();

        assertEquals(3, rows.size());

        RxJavaWebTarget target = server.getClientFactory().getClient("dao$testDomain$test", "1.0")
                .getInstance();

        RxJavaWebTarget childTarget = server.getClientFactory().getClient("dao$testDomain$testChild", "1.0")
                .getInstance();

        for(IViewQueryRow<String,String,TestDoc> row:rows){
            String id = row.getDocKey().split("/")[1];
            Response responseGet = target.path(id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .sync()
                    .get();
            TestDoc rsGetReadResult = responseGet.readEntity(new GenericType<>(TestDoc.class));
            String origStrVal = rsGetReadResult.strVal;
            assertEquals(responseGet.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_KEY), row.getDocKey());
            assertEquals(row.getKey(), rsGetReadResult.strVal);

            /*
            *   Read child list
             */
            Response childListResponseGet = childTarget.resolveTemplate("testDocId",id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .sync()
                    .get();
            List<SerializableViewQueryRow<String,String,TestDocChild>> responseList= childListResponseGet.readEntity(new GenericType<List<SerializableViewQueryRow<String, String, TestDocChild>>>(){});
            assertEquals(Long.parseLong(id) - 1, responseList.size());

            /*
            *   Read child item
             */
            Response childResponseGet = childTarget.resolveTemplate("testDocId",id).path("00001")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .sync()
                    .get();
            TestDocChild rsGetChildReadResult = childResponseGet.readEntity(new GenericType<>(TestDocChild.class));
            assertEquals("Child:0",rsGetChildReadResult.value);


            /*
            *  Create a new TestDoc
             */
            rsGetReadResult.strVal=origStrVal+" rest added";
            String oldKey = rsGetReadResult.getBaseMeta().getKey();
            rsGetReadResult.getBaseMeta().setKey(null);//Nullify Key to reuse read as input
            Response responsePost = target.request(MediaType.APPLICATION_JSON_TYPE).sync().post(Entity.entity(rsGetReadResult, MediaType.APPLICATION_JSON_TYPE));
            rsGetReadResult.getBaseMeta().setKey(oldKey);//Reset Key to reuse read as input
            String createdString = responsePost.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_KEY);
            TestDoc rsPostResult = responsePost.readEntity(new GenericType<>(TestDoc.class));
            assertEquals(rsGetReadResult.strVal,rsPostResult.strVal);

            //Create a new Child doc of the old one
            TestDocChild childDoc = new TestDocChild();
            childDoc.value = "Child of " + rsGetReadResult;
            String createdId = createdString.split("/")[1];

            Response responseCreateChildPost = childTarget.resolveTemplate("testDocId",createdId).request(MediaType.APPLICATION_JSON_TYPE).sync().post(Entity.entity(childDoc,MediaType.APPLICATION_JSON_TYPE));
            TestDocChild createdChildDoc = responseCreateChildPost.readEntity(TestDocChild.class);
            assertEquals(childDoc.value,createdChildDoc.value);

            /*
            *  Update the read TestDoc
             */
            rsGetReadResult.strVal=origStrVal+" updated";
            Response responsePut = target
                    .path(id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .sync()
                    .put(Entity.entity(rsGetReadResult, MediaType.APPLICATION_JSON_TYPE));
            TestDoc rsPutResult = responsePut.readEntity(new GenericType<>(TestDoc.class));
            assertEquals(responseGet.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_KEY), responsePut.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_KEY));
            assertNotEquals(responseGet.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_REV), responsePut.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_REV));
            assertEquals(rsGetReadResult.strVal, rsPutResult.strVal);

        }

        Response responseQuery = target
                .path("_queries/testview")
                .queryParam("startKey","test 3")
                .queryParam("endKey","test 6")
                .queryParam("inclusiveEndKey",false)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .sync()
                .get();


        responseQuery.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_QUERY_TOTAL_ROWS);
        List<SerializableViewQueryRow<String,String,TestDoc>> responseList= responseQuery.readEntity(new GenericType<List<SerializableViewQueryRow<String,String,TestDoc>>>(){});
        assertEquals(6, responseList.size());
        //TestDoc testDoc = new TestDoc();


    }

    @After
    public void endTest() throws Exception{
        if(server!=null){
            server.stop();
        }
        if(env!=null) {
            env.shutdown(true);
        }
        if(testUtils!=null){
            testUtils.stop();
        }
    }
}

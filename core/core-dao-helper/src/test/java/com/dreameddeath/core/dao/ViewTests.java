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

package com.dreameddeath.core.dao;

import com.dreameddeath.core.dao.helper.service.DaoHelperServiceUtils;
import com.dreameddeath.core.dao.helper.service.DaoServiceJacksonObjectMapper;
import com.dreameddeath.core.dao.helper.service.SerializableViewQueryRow;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentIntrospector;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import com.dreameddeath.testing.Utils;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import com.dreameddeath.testing.service.TestingRestServer;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by ceaj8230 on 18/12/2014.
 */
public class ViewTests {

    private Utils.TestEnvironment _env;
    private TestingRestServer _server;
    private CuratorTestUtils _testUtils;

    @Before
    public void initTest() throws  Exception{
        _env = new Utils.TestEnvironment("ViewTests");
        _env.addDocumentDao(new TestDao(), TestDoc.class);
        _env.addDocumentDao(new TestChildDao(),TestDocChild.class);
        _env.start();

        _testUtils = new CuratorTestUtils().prepare(1);
        _server = new TestingRestServer("serverTesting", _testUtils.getClient("serverTesting"),DaoServiceJacksonObjectMapper.getInstance(CouchbaseDocumentIntrospector.Domain.PUBLIC_SERVICE));
        TestDaoRestService service = new TestDaoRestService();
        service.setSessionFactory(_env.getSessionFactory());
        service.setUserFactory(new IUserFactory() {
            @Override
            public IUser validateFromToken(String token) {
                return null;
            }
        });
        _server.registerService("TestDaoRestService", service);


        TestChildDaoRestService childService = new TestChildDaoRestService();
        childService.setSessionFactory(_env.getSessionFactory());
        childService.setUserFactory(new IUserFactory() {
            @Override
            public IUser validateFromToken(String token) {
                return null;
            }
        });
        _server.registerService("TestChildDaoRestService", childService);


        _server.start();

    }

    @Test
    public void testView() throws Exception{
        ICouchbaseSession session = _env.getSessionFactory().newReadWriteSession(null);
        for(int i=0;i<10;++i){
            TestDoc doc = session.newEntity(TestDoc.class);
            doc.strVal="test "+i;
            doc.doubleVal=i*1.1;
            doc.longVal=i+1L;
            doc.intVal=i;
            doc.boolVal=(i%2==0)?true:false;
            doc.arrayVal = new ArrayList<>(i);
            for(int j=0;j<i;++j){
                TestDoc.TestDocSubElem elem=new TestDoc.TestDocSubElem();
                elem.longVal=j+1L;
                doc.arrayVal.add(elem);
            }
            session.save(doc);
            for(int j=0;j<i;++j) {
                TestDocChild child = session.newEntity(TestDocChild.class);
                child.parent= new TestDocLink(doc);
                child.value = String.format("Child:%d",j);
                session.save(child);
            }
        }

        IViewQuery<String,String,TestDoc> query = session.initViewQuery(TestDoc.class, "testView");
        query.withStartKey("test 3").withEndKey("test 6",false).withLimit(20).syncWithDoc();
        IViewQueryResult<String,String,TestDoc> result = session.executeQuery(query);
        List<IViewQueryRow<String,String,TestDoc>> rows = result.getAllRows();

        assertEquals(3, rows.size());

        WebTarget target = _server.getClientFactory().getClient("dao$testDomain$test", "1.0")
                .register(new JacksonJsonProvider(DaoServiceJacksonObjectMapper.getInstance(CouchbaseDocumentIntrospector.Domain.PUBLIC_SERVICE)));

        WebTarget childTarget = _server.getClientFactory().getClient("dao$testDomain$testChild", "1.0")
                .register(new JacksonJsonProvider(DaoServiceJacksonObjectMapper.getInstance(CouchbaseDocumentIntrospector.Domain.PUBLIC_SERVICE)));


        for(IViewQueryRow<String,String,TestDoc> row:rows){
            String id = row.getDocKey().split("/")[1];
            Response responseGet = target.path(id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();
            TestDoc rsGetReadResult = responseGet.readEntity(new GenericType<>(TestDoc.class));
            String origStrVal = rsGetReadResult.strVal;
            assertEquals(responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY),row.getDocKey());
            assertEquals(row.getKey(), rsGetReadResult.strVal);

            /*
            *   Read child list
             */
            Response childListResponseGet = childTarget.resolveTemplate("testDocId",id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();
            List<SerializableViewQueryRow<String,String,TestDocChild>> responseList= childListResponseGet.readEntity(new GenericType<List<SerializableViewQueryRow<String, String, TestDocChild>>>(){});
            assertEquals(Long.parseLong(id)-1,responseList.size());

            /*
            *   Read child item
             */
            Response childResponseGet = childTarget.resolveTemplate("testDocId",id).path("00001")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();
            TestDocChild rsGetChildReadResult = childResponseGet.readEntity(new GenericType<>(TestDocChild.class));
            assertEquals("Child:0",rsGetChildReadResult.value);


            /*
            *  Create a new TestDoc
             */
            rsGetReadResult.strVal=origStrVal+" rest added";
            Response responsePost = target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(rsGetReadResult, MediaType.APPLICATION_JSON_TYPE));
            String createdString = responsePost.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY);
            TestDoc rsPostResult = responsePost.readEntity(new GenericType<>(TestDoc.class));
            assertEquals(rsGetReadResult.strVal,rsPostResult.strVal);

            //Create a new Child doc of the old one
            TestDocChild childDoc = new TestDocChild();
            childDoc.value = "Child of " + rsGetReadResult;
            String createdId = createdString.split("/")[1];

            Response responseCreateChildPost = childTarget.resolveTemplate("testDocId",createdId).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(childDoc,MediaType.APPLICATION_JSON_TYPE));
            TestDocChild createdChildDoc = responseCreateChildPost.readEntity(TestDocChild.class);
            assertEquals(childDoc.value,createdChildDoc.value);

            /*
            *  Update the read TestDoc
             */
            rsGetReadResult.strVal=origStrVal+" updated";
            Response responsePut = target
                    .path(id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV, responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV))
                    .put(Entity.entity(rsGetReadResult, MediaType.APPLICATION_JSON_TYPE));
            TestDoc rsPutResult = responsePut.readEntity(new GenericType<>(TestDoc.class));
            assertEquals(responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY), responsePut.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY));
            assertNotEquals(responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV), responsePut.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV));
            assertEquals(rsGetReadResult.strVal, rsPutResult.strVal);

        }

        Response responseQuery = target
                .path("_queries/testview")
                .queryParam("startKey","test 3")
                .queryParam("endKey","test 6")
                .queryParam("inclusiveEndKey",false)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();


        responseQuery.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_QUERY_TOTAL_ROWS);
        List<SerializableViewQueryRow<String,String,TestDoc>> responseList= responseQuery.readEntity(new GenericType<List<SerializableViewQueryRow<String,String,TestDoc>>>(){});
        assertEquals(6,responseList.size());
        //TestDoc testDoc = new TestDoc();


    }

    @After
    public void endTest(){
        _env.shutdown(true);
    }
}

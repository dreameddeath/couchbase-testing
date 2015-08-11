/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.helper;

import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.helper.annotation.processor.DaoGeneratorAnnotationProcessor;
import com.dreameddeath.core.helper.service.AbstractDaoRestService;
import com.dreameddeath.core.helper.service.DaoHelperServiceUtils;
import com.dreameddeath.core.helper.service.DaoServiceJacksonObjectMapper;
import com.dreameddeath.core.helper.service.SerializableViewQueryRow;
import com.dreameddeath.core.model.annotation.processor.DocumentDefAnnotationProcessor;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentIntrospector;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import com.dreameddeath.testing.Utils;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import com.dreameddeath.testing.service.TestingRestServer;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import model.ITestDao;
import model.ITestDaoChild;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2015.
 */
public class DaoGenerationTests extends Assert {
    CouchbaseBucketSimulator _couchbaseClient;
    AnnotationProcessorTestingWrapper.Result _compiledEnv;

    private Utils.TestEnvironment _env;
    private TestingRestServer _server;
    private CuratorTestUtils _testUtils;

    public void initService(String className,String serviceName) throws Exception{
        AbstractDaoRestService service = (AbstractDaoRestService)_compiledEnv.getClass(className).newInstance();
        service.setSessionFactory(_env.getSessionFactory());
        service.setUserFactory(new IUserFactory() {
            @Override
            public IUser validateFromToken(String token) {
                return null;
            }
        });
        _server.registerService(serviceName, service);
    }

    @Before
    public void initTest() throws  Exception{
        _couchbaseClient = new CouchbaseBucketSimulator("test","test");
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new DaoGeneratorAnnotationProcessor()).
                withAnnotationProcessor(new DocumentDefAnnotationProcessor()).
                withTempDirectoryPrefix("DaoAnnotationProcessorTest");
        _compiledEnv= annotTester.run(this.getClass().getClassLoader().getResource("daoSourceFiles").getPath());
        assertTrue(_compiledEnv.getResult());

        _compiledEnv.updateSystemClassLoader();
        Constructor method = _compiledEnv.getConstructor(Utils.TestEnvironment.class.getName(), String.class, Utils.TestEnvironment.TestEnvType.class);
        _env = (Utils.TestEnvironment)method.newInstance("GeneratedDaoAndViewTests", Utils.TestEnvironment.TestEnvType.COUCHBASE);
        //_env = new Utils.TestEnvironment("GeneratedDaoAndViewTests", Utils.TestEnvironment.TestEnvType.COUCHBASE);
        _env.addDocumentDao((CouchbaseDocumentDao<CouchbaseDocument>) _compiledEnv.getClass("dao.TestDaoDao").newInstance());
        _env.addDocumentDao((CouchbaseDocumentDao<CouchbaseDocument>) _compiledEnv.getClass("dao.TestDaoChildDao").newInstance());
        _env.addDocumentDao((CouchbaseDocumentDao<CouchbaseDocument>) _compiledEnv.getClass("dao.TestDaoUidDao").newInstance());
        _env.start();

        _testUtils = new CuratorTestUtils().prepare(1);
        _server = new TestingRestServer("serverTesting", _testUtils.getClient("serverTesting"), DaoServiceJacksonObjectMapper.getInstance(CouchbaseDocumentIntrospector.Domain.PUBLIC_SERVICE));

        initService("service.TestDaoReadRestService", "TestDaoReadRestService");
        initService("service.TestDaoWriteRestService", "TestDaoWriteRestService");
        initService("service.TestDaoChildReadRestService", "TestDaoChildReadRestService");
        initService("service.TestDaoChildWriteRestService", "TestDaoChildWriteRestService");

        _server.start();

    }

    @Test
    public void runAnnotationProcessor() throws Exception {
        ICouchbaseSession session = _env.getSessionFactory().newReadWriteSession(null);
        Class<CouchbaseDocument> testDaoClass = _compiledEnv.getClass("model.TestDao");
        Class<CouchbaseDocument> testDaoChildClass = _compiledEnv.getClass("model.TestDaoChild");
        for(int i=0;i<10;++i){
            ITestDao doc = (ITestDao)session.newEntity(testDaoClass);
            doc.setValue("test "+i);
            session.save((CouchbaseDocument)doc);
            for(int j=0;j<i;++j) {
                ITestDaoChild child = (ITestDaoChild)session.newEntity(testDaoChildClass);
                child.setValue(String.format("Child:%d",j));
                child.setParentObjDao(doc);
                session.save((CouchbaseDocument)child);
            }
        }

        IViewQuery<String,String,CouchbaseDocument> query = session.initViewQuery(testDaoClass, "valueView");
        query.withStartKey("test 3").withEndKey("test 6",false).withLimit(20).syncWithDoc();
        IViewQueryResult<String,String,CouchbaseDocument> result = session.executeQuery(query);
        List<IViewQueryRow<String,String,CouchbaseDocument>> rows = result.getAllRows();

        assertEquals(3, rows.size());

        WebTarget readTarget = _server.getClientFactory().getClient("dao#test#daoProccessor$read", "1.0.0")
                .register(new JacksonJsonProvider(DaoServiceJacksonObjectMapper.getInstance(CouchbaseDocumentIntrospector.Domain.PUBLIC_SERVICE)));
        WebTarget writeTarget = _server.getClientFactory().getClient("dao#test#daoProccessor$write", "1.0.0")
                .register(new JacksonJsonProvider(DaoServiceJacksonObjectMapper.getInstance(CouchbaseDocumentIntrospector.Domain.PUBLIC_SERVICE)));

        WebTarget childReadTarget = _server.getClientFactory().getClient("dao#test#daoProccessorChild$read", "1.0.0")
                .register(new JacksonJsonProvider(DaoServiceJacksonObjectMapper.getInstance(CouchbaseDocumentIntrospector.Domain.PUBLIC_SERVICE)));
        WebTarget childWriteTarget = _server.getClientFactory().getClient("dao#test#daoProccessorChild$write", "1.0.0")
                .register(new JacksonJsonProvider(DaoServiceJacksonObjectMapper.getInstance(CouchbaseDocumentIntrospector.Domain.PUBLIC_SERVICE)));

        for(IViewQueryRow<String,String,CouchbaseDocument> row:rows){
            String id = row.getDocKey().split("/")[1];
            Response responseGet = readTarget.path(id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();
            ITestDao rsGetReadResult = responseGet.readEntity(new GenericType<>(testDaoClass));
            DaoHelperServiceUtils.finalizeFromResponse(responseGet,(CouchbaseDocument)rsGetReadResult);
            String origStrVal = rsGetReadResult.getValue();
            assertEquals(responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY), row.getDocKey());
            assertEquals(row.getKey(), rsGetReadResult.getValue());

            /*
            *   Read child list
             */
            Response childListResponseGet = childReadTarget.resolveTemplate("daoProccessorUid",id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();
            List<SerializableViewQueryRow<String,String,CouchbaseDocument>> responseList= childListResponseGet.readEntity(new GenericType<List<SerializableViewQueryRow<String, String, CouchbaseDocument>>>(){});
            assertEquals(Long.parseLong(id) - 1, responseList.size());

            /*
            *   Read child item
             */
            Response childResponseGet = childReadTarget.resolveTemplate("daoProccessorUid",id).path("0000000001")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get();
            ITestDaoChild rsGetChildReadResult = childResponseGet.readEntity(new GenericType<>(testDaoChildClass));
            assertEquals("Child:0",rsGetChildReadResult.getValue());


            /*
            *  Create a new TestDao
             */
            rsGetReadResult.setValue(origStrVal+" rest added");
            Response responsePost = writeTarget.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(rsGetReadResult, MediaType.APPLICATION_JSON_TYPE));
            String createdString = responsePost.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY);
            ITestDao rsPostResult = responsePost.readEntity(new GenericType<>(testDaoClass));
            assertEquals(rsGetReadResult.getValue(),rsPostResult.getValue());

            //Create a new Child doc of the old one
            ITestDaoChild childDoc = (ITestDaoChild)testDaoChildClass.newInstance();
            childDoc.setValue("Child of " + rsGetReadResult);
            childDoc.setParentObjDao(rsGetReadResult);
            String createdId = createdString.split("/")[1];

            Response responseCreateChildPost = childWriteTarget.resolveTemplate("daoProccessorUid",createdId).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(childDoc,MediaType.APPLICATION_JSON_TYPE));
            ITestDaoChild createdChildDoc = responseCreateChildPost.readEntity(new GenericType<>(testDaoChildClass));
            assertEquals(childDoc.getValue(),createdChildDoc.getValue());

            /*
            *  Update the read TestDoc
             */
            rsGetReadResult.setValue(origStrVal+" updated");
            Response responsePut = writeTarget
                    .path(id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV, responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV))
                    .put(Entity.entity(rsGetReadResult, MediaType.APPLICATION_JSON_TYPE));
            ITestDao rsPutResult = responsePut.readEntity(new GenericType<>(testDaoClass));
            assertEquals(responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY), responsePut.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY));
            assertNotEquals(responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV), responsePut.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV));
            assertEquals(rsGetReadResult.getValue(), rsPutResult.getValue());

        }


    }

    @After
    public void cleanupTest()throws Exception{
        if(_couchbaseClient!=null) {
            _couchbaseClient.shutdown();
        }
        if(_compiledEnv!=null) {
            _compiledEnv.cleanUp();
        }

        if(_server!=null){
            try {
                _server.stop();
            }
            catch(Exception e){
                //ignore
            }
        }
        if(_env!=null){
            _env.shutdown(true);
        }
    }


}

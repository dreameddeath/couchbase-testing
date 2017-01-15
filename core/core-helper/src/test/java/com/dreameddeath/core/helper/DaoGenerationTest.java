/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.helper;

import com.dreameddeath.core.couchbase.annotation.processor.CouchbaseAnnotationProcessor;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.helper.annotation.processor.DaoGeneratorAnnotationProcessor;
import com.dreameddeath.core.helper.service.SerializableViewQueryRow;
import com.dreameddeath.core.json.JsonProviderFactory;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.annotation.processor.DocumentDefAnnotationProcessor;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.service.client.rest.rxjava.RxJavaWebTarget;
import com.dreameddeath.core.service.testing.TestingRestServer;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentObjectMapperConfigurator;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentProviderInterceptor;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import com.dreameddeath.testing.Utils;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import model.ITestDao;
import model.ITestDaoChild;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2015.
 */
public class DaoGenerationTest extends Assert {
    AnnotationProcessorTestingWrapper.Result compiledEnv;

    private Utils.TestEnvironment env;
    private TestingRestServer server;
    private CuratorTestUtils testUtils;

    /*public void initService(String className,String serviceName) throws Exception{
        AbstractDaoRestService service = (AbstractDaoRestService)compiledEnv.getClass(className).newInstance();
        service.setSessionFactory(env.getSessionFactory());
        service.setUserFactory(new StandardMockUserFactory());
        server.registerService(serviceName, service);
    }*/

    @Before
    public void initTest() throws  Exception{
        //couchbaseClient = new CouchbaseBucketSimulator("test");
        //couchbaseClient.startDocument();
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new DaoGeneratorAnnotationProcessor()).
                withAnnotationProcessor(new DocumentDefAnnotationProcessor()).
                withAnnotationProcessor(new CouchbaseAnnotationProcessor()).
                withTempDirectoryPrefix("DaoAnnotationProcessorTest");
        compiledEnv= annotTester.run(this.getClass().getClassLoader().getResource("daoSourceFiles").getPath());
        assertTrue(compiledEnv.getResult());
        compiledEnv.updateSystemClassLoader();
        Constructor method = compiledEnv.getConstructor(Utils.TestEnvironment.class.getName(), String.class, Utils.TestEnvironment.TestEnvType.class);
        env = (Utils.TestEnvironment)method.newInstance("GeneratedDaoAndViewTests", Utils.TestEnvironment.TestEnvType.COUCHBASE);
        //_env = new Utils.TestEnvironment("GeneratedDaoAndViewTests", Utils.TestEnvironment.TestEnvType.COUCHBASE);
        env.addDocumentDao((CouchbaseDocumentDao<CouchbaseDocument>) compiledEnv.getClass("dao.TestGeneratedDaoDao").newInstance());
        env.addDocumentDao((CouchbaseDocumentDao<CouchbaseDocument>) compiledEnv.getClass("dao.TestGeneratedDaoChildDao").newInstance());
        env.addDocumentDao((CouchbaseDocumentDao<CouchbaseDocument>) compiledEnv.getClass("dao.TestGeneratedDaoUidDao").newInstance());
        env.start();

        testUtils = new CuratorTestUtils().prepare(1);
        server = new TestingRestServer("serverTesting", testUtils.getClient("serverTesting"), ObjectMapperFactory.BASE_INSTANCE.getMapper(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC));
        server.registerBeanObject("couchbaseSessionFactory",env.getSessionFactory());
        //server.registerBeanObject("userFactory",new StandardMockUserFactory());
        server.registerBeanClass("TestGeneratedDaoReadRestService",compiledEnv.getClass("service.TestGeneratedDaoReadRestService"));
        server.registerBeanClass("TestGeneratedDaoWriteRestService",compiledEnv.getClass("service.TestGeneratedDaoWriteRestService"));
        server.registerBeanClass("TestGeneratedDaoChildReadRestService",compiledEnv.getClass("service.TestGeneratedDaoChildReadRestService"));
        server.registerBeanClass("TestGeneratedDaoChildWriteRestService",compiledEnv.getClass("service.TestGeneratedDaoChildWriteRestService"));

        server.start();

    }

    @Test
    public void runAnnotationProcessor() throws Exception {
        ICouchbaseSession session = env.getSessionFactory().newReadWriteSession(null);
        Class<CouchbaseDocument> testGeneratedDaoClass = compiledEnv.getClass("model.TestGeneratedDao");
        Class<CouchbaseDocument> testGeneratedDaoChildClass = compiledEnv.getClass("model.TestGeneratedDaoChild");
        for(int i=0;i<10;++i){
            ITestDao doc = (ITestDao)session.newEntity(testGeneratedDaoClass);
            doc.setValue("test "+i);
            session.toBlocking().blockingSave((CouchbaseDocument)doc);
            for(int j=0;j<i;++j) {
                ITestDaoChild child = (ITestDaoChild)session.newEntity(testGeneratedDaoChildClass);
                child.setValue(String.format("Child:%d",j));
                child.setParentObjDao(doc);
                session.toBlocking().blockingSave((CouchbaseDocument)child);
            }
        }

        IViewQuery<String,String,CouchbaseDocument> query = session.initViewQuery(testGeneratedDaoClass, "valueView");
        query.withStartKey("test 3").withEndKey("test 6",false).withLimit(20).syncWithDoc();
        IViewQueryResult<String,String,CouchbaseDocument> result = session.executeQuery(query);
        List<IViewQueryRow<String,String,CouchbaseDocument>> rows = result.getAllRows();

        assertEquals(3, rows.size());

        RxJavaWebTarget readTarget = server.getClientFactory().getClient("dao#test#daoProccessor$read", "1.0.0")
                .getInstance()
                .register(JsonProviderFactory.getProvider(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC));
        RxJavaWebTarget writeTarget = server.getClientFactory().getClient("dao#test#daoProccessor$write", "1.0.0")
                .getInstance()
                .register(JsonProviderFactory.getProvider(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC));

        RxJavaWebTarget childReadTarget = server.getClientFactory().getClient("dao#test#daoProccessorChild$read", "1.0.0")
                .getInstance()
                .register(JsonProviderFactory.getProvider(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC));
        RxJavaWebTarget childWriteTarget = server.getClientFactory().getClient("dao#test#daoProccessorChild$write", "1.0.0")
                .getInstance()
                .register(JsonProviderFactory.getProvider(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC));

        for(IViewQueryRow<String,String,CouchbaseDocument> row:rows){
            String id = row.getDocKey().split("/")[1];
            Response responseGet = readTarget.path(id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .sync()
                    .get();
            ITestDao rsGetReadResult = responseGet.readEntity(new GenericType<>(testGeneratedDaoClass));
            //DaoHelperServiceUtils.finalizeFromResponse(responseGet,(CouchbaseDocument)rsGetReadResult);
            String origStrVal = rsGetReadResult.getValue();
            assertEquals(responseGet.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_KEY), row.getDocKey());
            assertEquals(row.getKey(), rsGetReadResult.getValue());

            /*
            *   Read child list
             */
            Response childListResponseGet = childReadTarget.resolveTemplate("daoProccessorUid",id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .sync()
                    .get();
            List<SerializableViewQueryRow<String,String,CouchbaseDocument>> responseList= childListResponseGet.readEntity(new GenericType<List<SerializableViewQueryRow<String, String, CouchbaseDocument>>>(){});
            assertEquals(Long.parseLong(id) - 1, responseList.size());

            /*
            *   Read child item
             */
            Response childResponseGet = childReadTarget.resolveTemplate("daoProccessorUid",id).path("0000000001")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .sync()
                    .get();
            ITestDaoChild rsGetChildReadResult = childResponseGet.readEntity(new GenericType<>(testGeneratedDaoChildClass));
            assertEquals("Child:0",rsGetChildReadResult.getValue());


            /*
            *  Create a new TestGeneratedDao
             */
            rsGetReadResult.setValue(origStrVal+" rest added");
            String oldKey = ((CouchbaseDocument)rsGetReadResult).getBaseMeta().getKey();//
            ((CouchbaseDocument)rsGetReadResult).getBaseMeta().setKey(null);
            Response responsePost = writeTarget.request(MediaType.APPLICATION_JSON_TYPE).sync().post(Entity.entity(rsGetReadResult, MediaType.APPLICATION_JSON_TYPE));
            ((CouchbaseDocument)rsGetReadResult).getBaseMeta().setKey(oldKey);
            String createdString = responsePost.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_KEY);
            ITestDao rsPostResult = responsePost.readEntity(new GenericType<>(testGeneratedDaoClass));
            assertEquals(rsGetReadResult.getValue(),rsPostResult.getValue());

            //Create a new Child doc of the old one
            ITestDaoChild childDoc = (ITestDaoChild)testGeneratedDaoChildClass.newInstance();
            childDoc.setValue("Child of " + rsGetReadResult);
            childDoc.setParentObjDao(rsGetReadResult);
            String createdId = createdString.split("/")[1];

            Response responseCreateChildPost = childWriteTarget.resolveTemplate("daoProccessorUid",createdId).request(MediaType.APPLICATION_JSON_TYPE).sync().post(Entity.entity(childDoc,MediaType.APPLICATION_JSON_TYPE));
            ITestDaoChild createdChildDoc = responseCreateChildPost.readEntity(new GenericType<>(testGeneratedDaoChildClass));
            assertEquals(childDoc.getValue(),createdChildDoc.getValue());

            /*
            *  Update the read TestDoc
             */
            rsGetReadResult.setValue(origStrVal+" updated");
            Response responsePut = writeTarget
                    .path(id)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    //.header(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV, responseGet.getHeaderString(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV))
                    .sync()
                    .put(Entity.entity(rsGetReadResult, MediaType.APPLICATION_JSON_TYPE));
            ITestDao rsPutResult = responsePut.readEntity(new GenericType<>(testGeneratedDaoClass));
            assertEquals(responseGet.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_KEY), responsePut.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_KEY));
            assertNotEquals(responseGet.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_REV), responsePut.getHeaderString(CouchbaseDocumentProviderInterceptor.HTTP_HEADER_DOC_REV));
            assertEquals(rsGetReadResult.getValue(), rsPutResult.getValue());

            //TODO manage views
            //TODO manage elastic search
        }


    }

    @After
    public void cleanupTest()throws Exception{
        if(server!=null){
            try {
                server.stop();
            }
            catch(Exception e){
                //ignore
            }
        }
        if(env!=null){
            env.shutdown(true);
        }
        if(compiledEnv!=null) {
            compiledEnv.cleanUp();
        }
        //if(couchbaseClient!=null) {
        //    couchbaseClient.shutdown();
        //}
        if(testUtils!=null){
            testUtils.stop();
        }
    }


}

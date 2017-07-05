/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.query.service;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.model.dto.converter.DtoConverterFactory;
import com.dreameddeath.core.query.dao.TestModelDao;
import com.dreameddeath.core.query.factory.BaseRemoteQueryClientFactory;
import com.dreameddeath.core.query.factory.QueryServiceFactory;
import com.dreameddeath.core.query.model.TestModel;
import com.dreameddeath.core.query.model.published.query.TestModelResponse;
import com.dreameddeath.core.query.service.rest.RestQueryTestModelService;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.testing.TestingRestServer;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by christophe jeunesse on 19/06/2017.
 */
public class StandardQueryServiceTest {
    private static final String BASE_PATH = "testRest";
    private static TestingRestServer server;

    private static CuratorTestUtils curatorUtils;
    private static CouchbaseBucketSimulator cbSimulator;
    private static CuratorFramework curatorFramework;
    private static QueryServiceFactory queryServiceFactory;
    private static CouchbaseSessionFactory sessionFactory;
    private static QueryServiceFactory remoteQueryServiceFactory;

    @BeforeClass
    public static void initialise() throws Exception {
        curatorUtils = new CuratorTestUtils().prepare(1);
        curatorFramework = curatorUtils.getClient("TestServicesTest");

        server = new TestingRestServer("serverTesting", curatorFramework);

        cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        sessionFactory = new CouchbaseSessionFactory.Builder().build();
        sessionFactory.getDocumentDaoFactory().addDao(new TestModelDao().setClient(cbSimulator));

        DtoConverterFactory dtoConverterFactory = new DtoConverterFactory();
        queryServiceFactory = new QueryServiceFactory();
        queryServiceFactory.setDependencyInjector(new IDependencyInjector() {
            @Override
            public <T> T getBeanOfType(Class<T> clazz) {
                try {
                    T obj = clazz.newInstance();
                    if(obj instanceof QueryTestModelService){
                        ((QueryTestModelService)obj).setDtoConverterFactory(dtoConverterFactory);
                        ((QueryTestModelService)obj).setSessionFactory(sessionFactory);
                    }
                    return obj;
                }
                catch (Throwable e){
                    throw new RuntimeException(e);
                }
            }

            @Override
            public <T> T autowireBean(T bean, String beanName) {
                throw new RuntimeException("Not allowed");
            }
        });
        QueryTestModelService service = queryServiceFactory.addQueryService(QueryTestModelService.class);
        server.registerBeanObject("DtoConverterFactory",dtoConverterFactory);
        server.registerBeanClass("restService", RestQueryTestModelService.class);
        server.registerBeanObject("queryService", service);
        server.registerBeanObject("queryServiceFactory",queryServiceFactory);
        server.registerBeanObject("couchbaseSessionFactory",sessionFactory);
        server.start();
        Thread.sleep(100);

        remoteQueryServiceFactory = new QueryServiceFactory();
        RestServiceClientFactory clientFactory = new RestServiceClientFactory(server.getServiceDiscoverer());
        BaseRemoteQueryClientFactory remoteClientFactory = new BaseRemoteQueryClientFactory();
        remoteClientFactory.setClientFactory(clientFactory);
        remoteQueryServiceFactory.setRemoteClientFactory(remoteClientFactory);

    }

    @Test
    public void test() throws Exception{
        TestModel model = new TestModel();
        model.strValue = "testing value 1";
        ICouchbaseSession session = sessionFactory.newReadWriteSession("test", AnonymousUser.INSTANCE);
        model = session.toBlocking().blockingCreate(model);


        IQueryService<TestModelResponse> queryService = queryServiceFactory.getQueryService(TestModelResponse.class);
        TestModelResponse response = queryService.asyncGet(model.getBaseMeta().getKey(),AnonymousUser.INSTANCE).blockingGet();
        assertEquals(model.strValue,response.getStrValue());

        IQueryService<TestModelResponse> remoteQueryService = remoteQueryServiceFactory.getQueryService(TestModelResponse.class);
        TestModelResponse remoteResponse = remoteQueryService.asyncGet(model.getBaseMeta().getKey(),AnonymousUser.INSTANCE).blockingGet();
        assertEquals(model.strValue,remoteResponse.getStrValue());
    }

}
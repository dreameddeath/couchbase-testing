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

package com.dreameddeath.core.dao.annotation;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.couchbase.annotation.processor.CouchbaseAnnotationProcessor;
import com.dreameddeath.core.couchbase.impl.CouchbaseBucketFactory;
import com.dreameddeath.core.dao.annotation.processor.DaoAnnotationProcessor;
import com.dreameddeath.core.dao.annotation.processor.DaoGeneratorAnnotationProcessor;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.model.TestGeneratedDao;
import com.dreameddeath.core.dao.model.TestGeneratedDaoChild;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.model.annotation.processor.DocumentDefAnnotationProcessor;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.google.common.base.Preconditions;
import io.reactivex.Single;
import model.ITestDao;
import model.ITestDaoChild;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by Christophe Jeunesse on 14/04/2015.
 */
public class DaoGenerationTest extends Assert {
    AnnotationProcessorTestingWrapper.Result compiledEnv;
    CouchbaseDocumentDaoFactory daoFactory;

    @Before
    public void initTest() throws  Exception{
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new DaoGeneratorAnnotationProcessor()).
                withAnnotationProcessor(new DaoAnnotationProcessor()).
                withAnnotationProcessor(new DocumentDefAnnotationProcessor()).
                withAnnotationProcessor(new CouchbaseAnnotationProcessor()).
                withTempDirectoryPrefix("DaoAnnotationProcessorTest");
        compiledEnv= annotTester.run(this.getClass().getClassLoader().getResource("daoSourceFiles").getPath());
        assertTrue(compiledEnv.getResult());
        compiledEnv.updateSystemClassLoader();
        daoFactory = CouchbaseDocumentDaoFactory.builder()
                .withBucketFactory(new CouchbaseBucketFactorySimulator())
                .build();

        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("testDaoGenerationInlined").getName(), "testDaoGenerationBucketInlined");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("testDaoGeneration").getName(), "testDaoGenerationBucket");

        Class<CouchbaseDocument> testGeneratedDaoClassInlined = compiledEnv.getClass("model.TestGeneratedDao");
        Class<CouchbaseDocument> testGeneratedDaoChildClassInlined = compiledEnv.getClass("model.TestGeneratedDaoChild");
        List<CouchbaseDocumentDao> daoForRootClassInlined = daoFactory.addDaosForEffectiveDomainsEntity(testGeneratedDaoClassInlined);
        Preconditions.checkArgument(daoForRootClassInlined.size()==1 && daoForRootClassInlined.get(0).getClass().getAnnotation(DaoGenPluginAnnot.class)==null);
        Preconditions.checkArgument(daoForRootClassInlined.size()==1 && daoForRootClassInlined.get(0).getClass().getAnnotation(DaoGlobalGenPluginAnnot.class)!=null);
        List<CouchbaseDocumentDao> daoForChildClassInlined = daoFactory.addDaosForEffectiveDomainsEntity(testGeneratedDaoChildClassInlined);
        Preconditions.checkArgument(daoForChildClassInlined.size()==1 && daoForChildClassInlined.get(0).getClass().getAnnotation(DaoGenPluginAnnot.class)!=null);
        Preconditions.checkArgument(daoForChildClassInlined.size()==1 && daoForChildClassInlined.get(0).getClass().getAnnotation(DaoGlobalGenPluginAnnot.class)!=null);

        daoFactory.addDaosForEffectiveDomainsEntity(TestGeneratedDao.class);
        daoFactory.addDaosForEffectiveDomainsEntity(TestGeneratedDaoChild.class);


        assertNotNull(daoFactory.getDaoForClass("testDaoGenerationInlined",testGeneratedDaoClassInlined));
        assertNotNull(daoFactory.getDaoForClass("testDaoGenerationInlined",testGeneratedDaoChildClassInlined));

        ((CouchbaseBucketFactory)daoFactory.getBucketFactory()).start();
        daoFactory.getViewDaoFactory().initAllViews();
    }

    @Test
    public void runAnnotationProcessor() throws Exception {
        ICouchbaseSession mockedSession = mock(ICouchbaseSession.class);
        when(mockedSession.asyncBuildKey(any(CouchbaseDocument.class)))
                .thenAnswer(invocationOnMock ->
                        daoFactory.getDaoForClass("testDaoGeneration",(Class<CouchbaseDocument>)invocationOnMock.getArgument(0).getClass())
                        .asyncBuildKey((ICouchbaseSession)invocationOnMock.getMock(),invocationOnMock.getArgument(0))
                );
        when(mockedSession.asyncValidate(any(CouchbaseDocument.class)))
                .thenAnswer(invocationOnMock -> Single.just(invocationOnMock.getArgument(0)));
        when(mockedSession.asyncIncrCounter(any(String.class),anyLong()))
                .thenAnswer(invocationOnMock ->
                    daoFactory.getCounterDaoFactory()
                            .getDaoForKey("testDaoGeneration",invocationOnMock.getArgument(0))
                            .asyncIncrCounter((ICouchbaseSession)invocationOnMock.getMock(),invocationOnMock.getArgument(0),invocationOnMock.getArgument(1),false)
                );
        CouchbaseDocumentDao<TestGeneratedDao> daoRoot = daoFactory.getDaoForClass("testDaoGeneration",TestGeneratedDao.class);
        CouchbaseDocumentDao<TestGeneratedDaoChild> daoChild = daoFactory.getDaoForClass("testDaoGeneration",TestGeneratedDaoChild.class);
        for(int i=0;i<10;++i){
            ITestDao doc = new TestGeneratedDao();
            doc.setValue("test "+i);

            daoRoot.toBlocking().blockingCreate(mockedSession, (TestGeneratedDao) doc,false);
            for(int j=0;j<i;++j) {
                ITestDaoChild child = new TestGeneratedDaoChild();
                child.setValue(String.format("Child:%d",j));
                child.setParentObjDao(doc);
                daoChild.toBlocking().blockingCreate(mockedSession, (TestGeneratedDaoChild) child,false);
            }
        }
        CouchbaseViewDao<String,String,CouchbaseDocument> viewDao =  daoRoot.getViewDao("valueView");
        IViewQuery<String,String,CouchbaseDocument> query = viewDao.buildViewQuery(null);
        query.withStartKey("test 3").withEndKey("test 6",false).withLimit(20).syncWithDoc();
        {
            IViewQueryResult<String, String, CouchbaseDocument> result = viewDao.query(mockedSession, false, query);
            List<IViewQueryRow<String, String, CouchbaseDocument>> rows = result.getAllRows();
            assertEquals(3, rows.size());
        }

        {
            Single<IViewAsyncQueryResult<String, String, CouchbaseDocument>> result = viewDao.asyncQuery(mockedSession, false, query);
            List<IViewQueryRow<String, String, CouchbaseDocument>> rows =result.flatMapObservable(IViewAsyncQueryResult::getRows)
                    .toList()
                    .blockingGet();
            assertEquals(3, rows.size());
        }
    }

    @After
    public void cleanupTest()throws Exception{
        if(compiledEnv!=null) {
            compiledEnv.cleanUp();
        }
    }


}

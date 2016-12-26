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

package com.dreameddeath.core.curator;

import com.dreameddeath.core.curator.discovery.impl.PathCuratorDiscoveryImpl;
import com.dreameddeath.core.curator.discovery.impl.StandardCuratorDiscoveryImpl;
import com.dreameddeath.core.curator.discovery.path.ICuratorPathDiscovery;
import com.dreameddeath.core.curator.discovery.path.ICuratorPathDiscoveryListener;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscovery;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscoveryListener;
import com.dreameddeath.core.curator.exception.DuplicateClusterClientException;
import com.dreameddeath.core.curator.model.IRegisterable;
import com.dreameddeath.core.curator.model.IRegistrablePathData;
import com.dreameddeath.core.curator.registrar.ICuratorPathRegistrar;
import com.dreameddeath.core.curator.registrar.ICuratorRegistrar;
import com.dreameddeath.core.curator.registrar.impl.CuratorPathRegistrarImpl;
import com.dreameddeath.core.curator.registrar.impl.CuratorRegistrarImpl;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CuratorFrameworkFactoryTest extends Assert{
    private final static Logger LOG = LoggerFactory.getLogger(CuratorFrameworkFactoryTest.class);
    private static int TIMEOUT_DURATION =5;
    TestingCluster testingCluster=null;

    @Before
    public void prepare() throws Exception{
        System.setProperty("zookeeper.jmx.log4j.disable","true");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<TestingCluster> future = executor.submit(() -> {
            try {
                TestingCluster cluster= new TestingCluster(3);
                cluster.start();
                return cluster;
            }
            catch(Exception e){
                LOG.error("Cannot start",e);
                throw new RuntimeException(e);
                //return null;
            }
        });
        testingCluster = future.get(1,TimeUnit.MINUTES);
        executor.shutdownNow();
    }

    @Test
    public void testStandardInit() throws Exception{
        String connectionString = testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);
        client.close();
    }

    @Test
    public void testDuplicate() throws Exception{
        String connectionString = testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);
        String[] servers = connectionString.split(CuratorFrameworkFactory.CONNECTION_STRING_SEPARATOR);
        try{
            CuratorFramework framework =CuratorFrameworkFactory.newClientInstance(servers[2] + "," + servers[1], new ExponentialBackoffRetry(1000, 3));
            framework.close();
            fail("The duplicate exception hasn't been raised");
        }
        catch(DuplicateClusterClientException e){
            assertEquals(client,e.getExistingFramework());
        }
        client.close();
    }


    @Test
    public void testNameSpace() throws Exception{
        String connectionString = testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient("prefix",connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);

        final String refValue="a testing String";
        client.create().forPath("/subPath",refValue.getBytes("UTF-8"));

        CuratorFramework rawClient = org.apache.curator.framework.CuratorFrameworkFactory.builder().connectString(connectionString).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();
        rawClient.start();
        rawClient.blockUntilConnected(TIMEOUT_DURATION,TimeUnit.SECONDS);
        assertEquals(refValue, new String(rawClient.getData().forPath("/prefix/subPath"),"UTF-8"));
        client.close();
        rawClient.close();
    }

    @Test
    public void testRegistrar() throws Exception{
        String connectionString = testingCluster.getConnectString();
        final CuratorFramework client = CuratorFrameworkFactory.newClient("testRegistrar",connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);

        ICuratorDiscovery<TestRegistrarClass> discovery = new StandardCuratorDiscoveryImpl<TestRegistrarClass>(client,"test/subpath"){
            @Override
            protected TestRegistrarClass deserialize(String uid,byte[] element) {
                return new TestRegistrarClass(uid,new String(element, Charset.defaultCharset()));
            }
        };

        final AtomicInteger errors=new AtomicInteger(0);
        final AtomicInteger nbRegister=new AtomicInteger(0);
        final AtomicInteger nbUnRegister=new AtomicInteger(0);
        final AtomicInteger nbUpdate=new AtomicInteger(0);
        final TestRegistrarClass firstObj = new TestRegistrarClass(UUID.randomUUID(),"first:value1","first:value2");
        final TestRegistrarClass firstObjUpdated = new TestRegistrarClass(firstObj.uid,"first:value1_updated","first:value2");
        final TestRegistrarClass secondObj = new TestRegistrarClass(UUID.randomUUID(),"second:value1","second:value2");
        final TestRegistrarClass thirdObj = new TestRegistrarClass(UUID.randomUUID(),"third:value1","third:value2");

        final Object lock=new Object();
        final ICuratorDiscoveryListener<TestRegistrarClass> listener = new ICuratorDiscoveryListener<TestRegistrarClass>() {
            private List<TestRegistrarClass> listchecksRegister =new ArrayList<>(Arrays.asList(firstObj,secondObj));
            private List<TestRegistrarClass> listchecksUnRegister =new ArrayList<>(Arrays.asList(secondObj,thirdObj));

            @Override
            public void onRegister(String uid, TestRegistrarClass obj) {
                synchronized (lock) {
                    LOG.info("Received registered {} of {}", uid, obj);
                    nbRegister.incrementAndGet();
                    try {
                        TestRegistrarClass refObj = null;
                        if (listchecksRegister.size() > 0) {
                            for (TestRegistrarClass objClass : listchecksRegister) {
                                if (objClass.getUid().equals(obj.getUid())) {
                                    refObj = obj;
                                }
                            }
                            assertNotNull(refObj);
                            assertTrue(listchecksRegister.remove(refObj));
                        } else {
                            refObj = thirdObj;
                        }

                        assertEquals(refObj.value1, obj.value1);
                        assertEquals(refObj.value2, obj.value2);
                        assertEquals(refObj.uid.toString(), obj.uid.toString());
                        assertEquals(refObj.uid.toString(), uid);
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        LOG.error("Register issue", e);
                    }
                    lock.notify();
                }
            }

            @Override
            public void onUnregister(String uid, TestRegistrarClass oldObj) {
                synchronized (lock) {
                    LOG.info("Received un-registered {} of {}", uid, oldObj);
                    int pos = nbUnRegister.incrementAndGet();
                    try {
                        if (pos == 1) {
                            assertEquals(firstObjUpdated.value1, oldObj.value1);
                            assertEquals(firstObjUpdated.value2, oldObj.value2);
                            assertEquals(firstObjUpdated.uid.toString(), oldObj.uid.toString());
                            assertEquals(firstObjUpdated.uid.toString(), uid);
                        } else {
                            LOG.info("Removing from {} / {}", listchecksUnRegister, oldObj);
                            assertTrue(listchecksUnRegister.remove(oldObj));
                        }
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        LOG.error("Unregister issue", e);
                    }

                    lock.notify();
                }
            }

            @Override
            public void onUpdate(String uid, TestRegistrarClass obj, TestRegistrarClass newObj) {
                synchronized (lock) {
                    LOG.info("Received update {} of {}", uid, obj);
                    nbUpdate.incrementAndGet();
                    try {
                        assertEquals(firstObj.value1, obj.value1);
                        assertEquals(firstObj.value2, obj.value2);
                        assertEquals(firstObj.uid.toString(), obj.uid.toString());
                        assertEquals(firstObj.uid.toString(), uid);

                        assertEquals(firstObjUpdated.value1, newObj.value1);
                        assertEquals(firstObjUpdated.value2, newObj.value2);
                        assertEquals(firstObjUpdated.uid.toString(), newObj.uid.toString());
                        assertEquals(firstObjUpdated.uid.toString(), uid);
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        LOG.error("Update issue", e);
                    }
                    lock.notify();
                }
            }
        };
        discovery.addListener(listener);
        ICuratorRegistrar<TestRegistrarClass> registrar=new CuratorRegistrarImpl<TestRegistrarClass>(client,"test/subpath") {
            @Override
            protected byte[] serialize(TestRegistrarClass obj) throws Exception {
                return obj.toString().getBytes(Charset.defaultCharset());
            }
        };

        registrar.register(firstObj);
        registrar.register(secondObj);
        discovery.start();
        assertEquals(2,nbRegister.get());

        synchronized (lock) {
            registrar.update(firstObjUpdated);
            lock.wait(10000, 0);
            assertEquals(1,nbUpdate.get());
            registrar.deregister(firstObj);
            lock.wait(10000, 0);
            assertEquals(1,nbUnRegister.get());
            registrar.register(thirdObj);
            lock.wait(10000, 0);
            assertEquals(3,nbRegister.get());
            registrar.close();
            lock.wait(10000,0);
            lock.wait(10000,0);
            assertEquals(3,nbUnRegister.get());
        }
        discovery.stop();
        client.close();
    }


    @Test
    public void testPathRegistrar() throws Exception{
        String connectionString = testingCluster.getConnectString();
        final CuratorFramework client = CuratorFrameworkFactory.newClient("testRegistrar",connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);

        ICuratorPathDiscovery<TestPathRegistrarClass> discovery = new PathCuratorDiscoveryImpl<TestPathRegistrarClass>(client,"test/subpath"){
            @Override
            protected TestPathRegistrarClass deserialize(String uid,byte[] element) {
                return new TestPathRegistrarClass(uid,new String(element, Charset.defaultCharset()));
            }
        };
        final AtomicInteger errors=new AtomicInteger(0);
        final AtomicInteger nbRegister=new AtomicInteger(0);
        final AtomicInteger nbUnRegister=new AtomicInteger(0);
        final AtomicInteger nbUnRegisterAtClosure=new AtomicInteger(0);
        final AtomicBoolean isClosure=new AtomicBoolean(false);
        final AtomicInteger nbUpdate=new AtomicInteger(0);
        final TestPathRegistrarClass firstObj = new TestPathRegistrarClass(UUID.randomUUID(),"first:value1","1.1.0");
        final TestPathRegistrarClass firstObjUpdatedUpperVersion = new TestPathRegistrarClass(firstObj.uid,"first:value1_updated","1.2.0");
        final TestPathRegistrarClass firstObjIgnoredLowerVersion = new TestPathRegistrarClass(firstObj.uid,"first:value1_ignored","0.9.0");
        final TestPathRegistrarClass secondObj = new TestPathRegistrarClass(UUID.randomUUID(),"second:value1","1.0.0");
        final TestPathRegistrarClass thirdObj = new TestPathRegistrarClass(UUID.randomUUID(),"third:value1","1.0.0");

        final Object lock=new Object();
        final ICuratorPathDiscoveryListener<TestPathRegistrarClass> listener = new ICuratorPathDiscoveryListener<TestPathRegistrarClass>() {
            private List<TestPathRegistrarClass> listchecksRegister =new ArrayList<>(Arrays.asList(firstObj,secondObj,thirdObj));
            private List<TestPathRegistrarClass> listchecksUnRegister =new ArrayList<>(Collections.singletonList(firstObj));
            private List<TestPathRegistrarClass> listchecksUnRegisterAtClosure =new ArrayList<>(Arrays.asList(secondObj,thirdObj));
            @Override
            public void onRegister(String uid, TestPathRegistrarClass obj) {
                synchronized (lock) {
                    LOG.info("Received registered {} of {}", uid, obj);
                    nbRegister.incrementAndGet();
                    try {
                        TestPathRegistrarClass refObj = null;
                        if (listchecksRegister.size() > 0) {
                            for (TestPathRegistrarClass objClass : listchecksRegister) {
                                if (objClass.uid().equals(obj.uid())) {
                                    refObj = obj;
                                }
                            }
                            assertNotNull(refObj);
                            assertTrue(listchecksRegister.remove(refObj));
                        } else {
                            refObj = thirdObj;
                        }

                        assertEquals(refObj.value1, obj.value1);
                        assertEquals(refObj.version(), obj.version());
                        assertEquals(refObj.uid.toString(), obj.uid.toString());
                        assertEquals(refObj.uid.toString(), uid);
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        LOG.error("Register issue", e);
                    }
                    lock.notify();
                }
            }

            @Override
            public void onUnregister(String uid, TestPathRegistrarClass oldObj) {
                synchronized (lock) {
                    LOG.info("Received un-registered {} of {}", uid, oldObj);
                    if(!isClosure.get()) {
                        int pos = nbUnRegister.incrementAndGet();
                        try {
                            if (pos == 1) {
                                assertEquals(firstObjUpdatedUpperVersion.value1, oldObj.value1);
                                assertEquals(firstObjUpdatedUpperVersion.version(), oldObj.version());
                                assertEquals(firstObjUpdatedUpperVersion.uid.toString(), oldObj.uid.toString());
                                assertEquals(firstObjUpdatedUpperVersion.uid.toString(), uid);
                            } else{
                                LOG.info("Removing from {} / {}", listchecksUnRegister, oldObj);
                                assertTrue(listchecksUnRegister.remove(oldObj));
                            }
                        } catch (Throwable e) {
                            errors.incrementAndGet();
                            LOG.error("Unregister issue", e);
                        }
                    }
                    else{
                        nbUnRegisterAtClosure.incrementAndGet();
                        try{
                            LOG.info("Removing from {} / {}", listchecksUnRegister, oldObj);
                            assertTrue(listchecksUnRegisterAtClosure.remove(oldObj));
                        }
                        catch (Throwable e) {
                            errors.incrementAndGet();
                            LOG.error("Unregister at cloure issue", e);
                        }
                    }
                    lock.notify();
                }
            }

            @Override
            public void onUpdate(String uid, TestPathRegistrarClass obj, TestPathRegistrarClass newObj) {
                synchronized (lock) {
                    LOG.info("Received update {} of {}", uid, obj);
                    nbUpdate.incrementAndGet();
                    try {
                        assertEquals(firstObj.value1, obj.value1);
                        assertEquals(firstObj.version(), obj.version());
                        assertEquals(firstObj.uid.toString(), obj.uid.toString());
                        assertEquals(firstObj.uid.toString(), uid);

                        assertEquals(firstObjUpdatedUpperVersion.value1, newObj.value1);
                        assertEquals(firstObjUpdatedUpperVersion.version(), newObj.version());
                        assertEquals(firstObjUpdatedUpperVersion.uid.toString(), newObj.uid.toString());
                        assertEquals(firstObjUpdatedUpperVersion.uid.toString(), uid);
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        LOG.error("Update issue", e);
                    }
                    lock.notify();
                }
            }
        };
        discovery.addListener(listener);
        ICuratorPathRegistrar<TestPathRegistrarClass> registrar=new CuratorPathRegistrarImpl<TestPathRegistrarClass>(client,"test/subpath") {
            @Override
            protected byte[] serialize(TestPathRegistrarClass obj) throws Exception {
                return obj.toString().getBytes(Charset.defaultCharset());
            }

            @Override
            protected TestPathRegistrarClass deserialize(String uid, byte[] currentData) {
                return new TestPathRegistrarClass(uid,new String(currentData,Charset.defaultCharset()));
            }

            @Override
            protected int compare(TestPathRegistrarClass sourceObj, TestPathRegistrarClass targetObj) {
                return sourceObj.version().compareTo(targetObj.version());
            }
        };

        assertEquals(ICuratorPathRegistrar.Result.DONE,registrar.register(firstObj));
        assertEquals(ICuratorPathRegistrar.Result.DONE,registrar.register(secondObj));
        discovery.start();
        assertEquals(2,nbRegister.get());

        synchronized (lock) {
            assertEquals(ICuratorPathRegistrar.Result.IGNORED,registrar.update(firstObjIgnoredLowerVersion));
            assertEquals(ICuratorPathRegistrar.Result.IGNORED,registrar.register(firstObjIgnoredLowerVersion));
            assertEquals(2L,registrar.registeredList().size());
            assertEquals(ICuratorPathRegistrar.Result.DONE,registrar.update(firstObjUpdatedUpperVersion));
            assertEquals(2L,registrar.registeredList().size());
            lock.wait(10000, 0);
            assertEquals(1,nbUpdate.get());
            assertEquals(ICuratorPathRegistrar.Result.IGNORED,registrar.deregister(firstObj));
            assertEquals(ICuratorPathRegistrar.Result.DONE,registrar.deregister(firstObjUpdatedUpperVersion));
            lock.wait(10000, 0);
            assertEquals(1,nbUnRegister.get());
            registrar.register(thirdObj);
            lock.wait(10000, 0);
            assertEquals(3,nbRegister.get());
            isClosure.compareAndSet(false,true);
            registrar.close();
        }
        discovery.stop();
        assertEquals(2,nbUnRegisterAtClosure.get());
        client.close();
        assertEquals(0L,errors.get());
    }

    @After
    public void endTest() throws Exception{
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CuratorFrameworkFactory.cleanup();


        Future<Boolean> future = executor.submit(() -> {
            try {
                testingCluster.stop();
                testingCluster.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        future.get(1,TimeUnit.MINUTES);
        executor.shutdownNow();
    }


    public static class TestPathRegistrarClass implements IRegistrablePathData{
        public final String value1;
        public String version;
        public final UUID uid;

        @Override
        public String uid() {
            return uid.toString();
        }

        public TestPathRegistrarClass(String uuid,String encoded){
            this.uid = UUID.fromString(uuid);
            String[] values=encoded.split("\\|");
            value1=values[0];
            version=values[1];
        }

        public TestPathRegistrarClass(UUID uid,String value1,String value2){
            this.uid = uid;
            this.value1 = value1;
            this.version = value2;
        }

        @Override
        public String toString(){
            return value1+"|"+version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestPathRegistrarClass that = (TestPathRegistrarClass) o;

            if (!value1.equals(that.value1)) return false;
            if (!version.equals(that.version)) return false;
            return uid.equals(that.uid);

        }

        @Override
        public int hashCode() {
            int result = value1.hashCode();
            result = 31 * result + version.hashCode();
            result = 31 * result + uid.hashCode();
            return result;
        }

        @Override
        public String version() {
            return version;
        }
    }

    public static class TestRegistrarClass implements IRegisterable{
        public final String value1;
        public String value2;
        public final UUID uid;

        @Override
        public String getUid() {
            return uid.toString();
        }

        public TestRegistrarClass(String uuid,String encoded){
            this.uid = UUID.fromString(uuid);
            String[] values=encoded.split("\\|");
            value1=values[0];
            value2=values[1];
        }

        public TestRegistrarClass(UUID uid,String value1,String value2){
            this.uid = uid;
            this.value1 = value1;
            this.value2 = value2;
        }

        @Override
        public String toString(){
            return value1+"|"+value2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestRegistrarClass that = (TestRegistrarClass) o;

            if (!value1.equals(that.value1)) return false;
            if (!value2.equals(that.value2)) return false;
            return uid.equals(that.uid);

        }

        @Override
        public int hashCode() {
            int result = value1.hashCode();
            result = 31 * result + value2.hashCode();
            result = 31 * result + uid.hashCode();
            return result;
        }
    }
}
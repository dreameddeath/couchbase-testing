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

package com.dreameddeath.core.couchbase.dcp;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 27/05/2015.
 */
public class CouchbaseDCPConnectorTest {
    private static Logger LOG = LoggerFactory.getLogger(CouchbaseDCPConnectorTest.class);

    public static class TestEventHandler implements DCPEventHandler{
        @Override
        public void onEvent(DCPEvent event, long sequence, boolean endOfBatch) throws Exception {
            LOG.debug("Event recieved {}",event.getType());
        }
    }
    public static class TestExceptionHandler implements DCPExceptionHandler{
        @Override
        public void handleEventException(Throwable ex, long sequence, Object event) {
            LOG.error("Event exception",ex);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            LOG.error("Start exception",ex);
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            LOG.error("End exception",ex);
        }
    }


    @Test
    public void testDcpConnection(){
        /*CouchbaseDCPEnvironment env = DefaultCouchbaseDCPEnvironment.builder().streamName(UUID.randomUUID().toString()).build();
        CouchbaseDCPConnector connector = new CouchbaseDCPConnector(env, Arrays.asList("localhost:8091"),"default","",new TestEventHandler(),new TestExceptionHandler());

        connector.connect();
        connector.run();*/



    }

}
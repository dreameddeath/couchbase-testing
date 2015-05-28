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

package com.dreameddeath.testing.couchbase.dcp;

import com.couchbase.client.core.message.dcp.MutationMessage;
import com.dreameddeath.core.couchbase.dcp.CouchbaseDCPConnector;
import com.dreameddeath.core.couchbase.dcp.CouchbaseDCPEnvironment;
import com.dreameddeath.core.couchbase.dcp.DCPEventHandler;
import com.dreameddeath.core.couchbase.dcp.DCPExceptionHandler;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.couchbase.DocumentSimulator;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 28/05/2015.
 */
public class CouchbaseDCPConnectorSimulator extends CouchbaseDCPConnector{
    private CouchbaseBucketSimulator _simulator;

    public CouchbaseDCPConnectorSimulator(CouchbaseDCPEnvironment environment,
                                          List<String> couchbaseNodes,
                                          String couchbaseBucket, String couchbasePassword,
                                          DCPEventHandler eventHandler, DCPExceptionHandler exceptionHandler,
                                          CouchbaseBucketSimulator simulator) {
        super(environment, couchbaseNodes, couchbaseBucket, couchbasePassword, eventHandler, exceptionHandler);
        _simulator = simulator;
    }

    @Override
    public void connect(final long timeout, final TimeUnit timeUnit) {
        // Do nothing
    }

    public void run() {
        _simulator.addCouchbaseDcpSimulator(this);
    }

    @Override
    public Boolean stop(){
        _simulator.removeCouchbaseDcpSimulator(this);
        return true;
    }
/*public MutationMessage(short partition, String key, ByteBuf content, int expiration,
                           int flags, int lockTime, long cas, String bucket) {
        this(partition, key, content, expiration, flags, lockTime, cas, bucket, null);
    */
    public void notifyUpdate(CouchbaseBucketSimulator.ImpactMode mode,DocumentSimulator doc){
        MutationMessage msg = new MutationMessage((short)0,doc.getKey(),doc.getData(),doc.getExpiry(),doc.getFlags(),0,doc.getCas(),getBucket());
        getDcpRingBuffer().tryPublishEvent(getTranslator(),msg);
    }
}

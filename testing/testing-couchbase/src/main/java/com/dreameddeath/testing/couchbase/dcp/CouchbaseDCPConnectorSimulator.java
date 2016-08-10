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

import com.couchbase.client.core.message.CouchbaseMessage;
import com.couchbase.client.core.message.dcp.MutationMessage;
import com.couchbase.client.core.message.dcp.RemoveMessage;
import com.dreameddeath.core.couchbase.dcp.CouchbaseDCPConnector;
import com.dreameddeath.core.couchbase.dcp.ICouchbaseDCPEnvironment;
import com.dreameddeath.core.couchbase.dcp.impl.AbstractDCPFlowHandler;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.couchbase.DocumentSimulator;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 28/05/2015.
 */
public class CouchbaseDCPConnectorSimulator extends CouchbaseDCPConnector{
    private CouchbaseBucketSimulator simulator;

    public CouchbaseDCPConnectorSimulator(ICouchbaseDCPEnvironment environment,
                                          List<String> couchbaseNodes,
                                          String couchbaseBucket, String couchbasePassword,
                                          AbstractDCPFlowHandler handler,
                                          CouchbaseBucketSimulator simulator) {
        super(environment, couchbaseNodes, couchbaseBucket, couchbasePassword, handler);
        this.simulator = simulator;
    }

    @Override
    public void connect(final long timeout, final TimeUnit timeUnit) {
        // Do nothing
    }

    public void run() {
        simulator.addCouchbaseDcpSimulator(this);
    }

    @Override
    public Boolean stop(){
        simulator.removeCouchbaseDcpSimulator(this);
        getDisruptor().shutdown();
        try {
            Thread.sleep(10);
        }
        catch(InterruptedException e){
            //Ignore
        }

        return true;
    }

    public void notifyUpdate(CouchbaseBucketSimulator.ImpactMode mode,DocumentSimulator doc){
        CouchbaseMessage msg=null;
        switch (mode){
            case DELETE:
                msg = new RemoveMessage(doc.getData().array().length,(short)0,doc.getKey(),doc.getCas(),/*sequence*/0,/*revSeq*/0,getBucket());
                break;
            case ADD:
            case APPEND:
            case PREPEND:
            case REPLACE:
            case UPDATE:
                msg = new MutationMessage(doc.getData().array().length,(short)0,doc.getKey(),doc.getData(),doc.getExpiry(),/*sequence*/0,/*revSeq*/0,doc.getFlags(),/*lockTime*/0,doc.getCas(),getBucket());
                break;
        }
        getDcpRingBuffer().tryPublishEvent(getTranslator(),msg);
    }
}

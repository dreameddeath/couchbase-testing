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

package com.dreameddeath.core.couchbase.dcp;

import com.couchbase.client.core.ClusterFacade;
import com.couchbase.client.core.CouchbaseCore;
import com.couchbase.client.core.endpoint.dcp.DCPConnection;
import com.couchbase.client.core.message.CouchbaseMessage;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.core.message.cluster.*;
import com.couchbase.client.core.message.dcp.OpenConnectionRequest;
import com.couchbase.client.core.message.dcp.OpenConnectionResponse;
import com.couchbase.client.core.message.kv.MutationToken;
import com.couchbase.client.deps.com.lmax.disruptor.EventTranslatorOneArg;
import com.couchbase.client.deps.com.lmax.disruptor.RingBuffer;
import com.couchbase.client.deps.com.lmax.disruptor.dsl.Disruptor;
import com.couchbase.client.deps.io.netty.util.concurrent.DefaultThreadFactory;
import com.couchbase.client.java.ConnectionString;
import com.dreameddeath.core.couchbase.dcp.impl.AbstractDCPFlowHandler;
import com.dreameddeath.core.couchbase.rxjava.RxJavaWrapper;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class CouchbaseDCPConnector implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CouchbaseDCPConnector.class);
    private static final DCPEventFactory DCP_EVENT_FACTORY = new DCPEventFactory();
    public static final int MAX_DCP_SEQUENCE = 0xffffffff;

    private final ClusterFacade core;
    private final AbstractDCPFlowHandler flowHandler;
    private final RingBuffer<DCPEvent> dcpRingBuffer;
    private final Disruptor<DCPEvent> disruptor;
    private final List<String> nodes;
    private final String bucket;
    private final String streamName;
    private final String password;
    private final EventTranslatorOneArg<DCPEvent, CouchbaseMessage> translator;
    private DCPConnection dcpConnection;


    protected RingBuffer<DCPEvent> getDcpRingBuffer(){
        return dcpRingBuffer;
    }
    protected Disruptor<DCPEvent> getDisruptor(){return disruptor;}

    protected EventTranslatorOneArg<DCPEvent, CouchbaseMessage>  getTranslator(){
        return translator;
    }

    public String getBucket(){
        return bucket;
    }


    public CouchbaseDCPConnector(final ICouchbaseDCPEnvironment environment,
                                 final List<String> couchbaseNodes,
                                 final String couchbaseBucket, final String couchbasePassword,
                                 final AbstractDCPFlowHandler flowHandler){
        this(environment, couchbaseNodes,new CouchbaseCore(environment), couchbaseBucket, couchbasePassword, flowHandler);
    }

    public CouchbaseDCPConnector(final ICouchbaseDCPEnvironment environment,
                                  final List<String> couchbaseNodes,
                                  final ClusterFacade core,
                                  final String couchbaseBucket, final String couchbasePassword,
                                  final AbstractDCPFlowHandler flowHandler
    ) {
        this.flowHandler = flowHandler;
        streamName = environment.streamName();
        nodes = couchbaseNodes;
        bucket = couchbaseBucket;
        password = couchbasePassword;
        this.core = core;
        ExecutorService disruptorExecutor = Executors.newFixedThreadPool(environment.threadPoolSize(), new DefaultThreadFactory(environment.threadPoolName(), true));
        disruptor = new Disruptor<>(
                DCP_EVENT_FACTORY,
                environment.eventBufferSize(),
                disruptorExecutor
        );

        disruptor.handleEventsWith(flowHandler.getEventHandler());
        disruptor.setDefaultExceptionHandler(flowHandler.getExceptionHandler());
        disruptor.start();
        dcpRingBuffer = disruptor.getRingBuffer();

        translator = (event, sequence, message) -> event.setMessage(message);
    }


    public void connect() {
        connect(2, TimeUnit.SECONDS);
    }

    public void connect(final long timeout, final TimeUnit timeUnit) {
        ConnectionString connectionString = ConnectionString.fromHostnames(nodes);
        List<String> seedNodes = connectionString.hosts().stream().map(InetSocketAddress::getHostName).collect(Collectors.toList());
        OpenConnectionResponse response = core.<SeedNodesResponse>send(new SeedNodesRequest(seedNodes))
                .flatMap(result-> core.<OpenBucketResponse>send(new OpenBucketRequest(bucket, password)))
                .flatMap(result->core.<OpenConnectionResponse>send(new OpenConnectionRequest(streamName, bucket)))
                .timeout(timeout, timeUnit)
                .toBlocking()
                .single();

        this.dcpConnection = response.connection();
    }

    /**
     * Executes worker reading loop, which relays events from Couchbase to Any "client".
     */
    public void run() {
        RxJavaWrapper.toRxJava2Observable(dcpConnection.getCurrentState())
                .flatMap(this::requestStreams)
                .toList()
                .flatMapObservable(list->RxJavaWrapper.toRxJava2Observable(dcpConnection.subject()))
                .blockingIterable()
                .forEach(dcpRequest -> dcpRingBuffer.tryPublishEvent(CouchbaseDCPConnector.this.translator, dcpRequest));

    }

    private Observable<ResponseStatus> requestStreams(final MutationToken token) {
        long startSequenceNumber = 0;
        AbstractDCPFlowHandler.LastSnapshotReceived lastSnapshotReceived = flowHandler.getLastSnapshot(bucket, (short)token.vbucketID());
        if(lastSnapshotReceived!=null) {
            startSequenceNumber = lastSnapshotReceived.getSequenceNumber();
        }
        return RxJavaWrapper.toRxJava2Observable(dcpConnection.addStream(
                    (short) token.vbucketID(),
                    token.vbucketUUID(),
                    startSequenceNumber,MAX_DCP_SEQUENCE,
                    startSequenceNumber,MAX_DCP_SEQUENCE
                ));


    }

    public Boolean stop(){
        return RxJavaWrapper.toRxJava2Single(core.send(new CloseBucketRequest(bucket)))
                .map(reponse->reponse.status().isSuccess()).blockingGet();
    }
}

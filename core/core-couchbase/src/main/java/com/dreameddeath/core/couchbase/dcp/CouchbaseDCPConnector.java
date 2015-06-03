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

import com.couchbase.client.core.ClusterFacade;
import com.couchbase.client.core.CouchbaseCore;
import com.couchbase.client.core.config.CouchbaseBucketConfig;
import com.couchbase.client.core.message.CouchbaseMessage;
import com.couchbase.client.core.message.cluster.*;
import com.couchbase.client.core.message.dcp.DCPRequest;
import com.couchbase.client.core.message.dcp.OpenConnectionRequest;
import com.couchbase.client.core.message.dcp.StreamRequestRequest;
import com.couchbase.client.core.message.dcp.StreamRequestResponse;
import com.couchbase.client.deps.com.lmax.disruptor.EventTranslatorOneArg;
import com.couchbase.client.deps.com.lmax.disruptor.RingBuffer;
import com.couchbase.client.deps.com.lmax.disruptor.dsl.Disruptor;
import com.couchbase.client.deps.io.netty.util.concurrent.DefaultThreadFactory;
import com.couchbase.client.java.ConnectionString;
import com.dreameddeath.core.couchbase.dcp.impl.AbstractDCPFlowHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

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

    private final ClusterFacade _core;
    private final AbstractDCPFlowHandler _flowHandler;
    private final RingBuffer<DCPEvent> _dcpRingBuffer;
    private final Disruptor<DCPEvent> _disruptor;
    private final List<String> _nodes;
    private final String _bucket;
    private final String _streamName;
    private final String _password;
    private final EventTranslatorOneArg<DCPEvent, CouchbaseMessage> _translator;


    protected RingBuffer<DCPEvent> getDcpRingBuffer(){
        return _dcpRingBuffer;
    }
    protected Disruptor<DCPEvent> getDisruptor(){return _disruptor;}

    protected EventTranslatorOneArg<DCPEvent, CouchbaseMessage>  getTranslator(){
        return _translator;
    }

    public String getBucket(){
        return _bucket;
    }


    public CouchbaseDCPConnector(final ICouchbaseDCPEnvironment environment,
                                  final List<String> couchbaseNodes,
                                  final String couchbaseBucket, final String couchbasePassword,
                                  final AbstractDCPFlowHandler flowHandler
    ) {
        _flowHandler = flowHandler;
        _streamName = environment.streamName();
        _nodes = couchbaseNodes;
        _bucket = couchbaseBucket;
        _password = couchbasePassword;
        _core = new CouchbaseCore(environment);
        ExecutorService disruptorExecutor = Executors.newFixedThreadPool(environment.threadPoolSize(), new DefaultThreadFactory(environment.threadPoolName(), true));
        _disruptor = new Disruptor<>(
                DCP_EVENT_FACTORY,
                environment.eventBufferSize(),
                disruptorExecutor
        );

        _disruptor.handleEventsWith(flowHandler.getEventHandler());
        _disruptor.handleExceptionsWith(flowHandler.getExceptionHandler());
        _disruptor.start();
        _dcpRingBuffer = _disruptor.getRingBuffer();

        _translator = (event, sequence, message) -> event.setMessage(message);
    }


    public void connect() {
        connect(2, TimeUnit.SECONDS);
    }

    public void connect(final long timeout, final TimeUnit timeUnit) {
        ConnectionString connectionString = ConnectionString.fromHostnames(_nodes);
        List<String> seedNodes = connectionString.hosts().stream().map(InetSocketAddress::getHostName).collect(Collectors.toList());
        _core.send(new SeedNodesRequest(seedNodes))
                .timeout(timeout, timeUnit)
                .toBlocking()
                .single();
        _core.send(new OpenBucketRequest(_bucket, _password))
                .timeout(timeout, timeUnit)
                .toBlocking()
                .single();

    }

    /**
     * Executes worker reading loop, which relays events from Couchbase to Any "client".
     */
    public void run() {
        _core.send(new OpenConnectionRequest(_streamName, _bucket))
                .toList()
                .flatMap(couchbaseResponses -> CouchbaseDCPConnector.this.partitionSize())
                .flatMap(numberOfPartitions -> requestStreams(numberOfPartitions))
                .toBlocking()
                .forEach(dcpRequest -> _dcpRingBuffer.tryPublishEvent(CouchbaseDCPConnector.this._translator, dcpRequest));

    }

    private Observable<Integer> partitionSize() {
        return _core
                .<GetClusterConfigResponse>send(new GetClusterConfigRequest())
                .map(response->((CouchbaseBucketConfig) response.config().bucketConfig(_bucket)).numberOfPartitions());

    }

    private Observable<DCPRequest> requestStreams(final int numberOfPartitions) {
        return Observable.merge(
                Observable.range(0, numberOfPartitions)
                        .flatMap(partition -> _core.<StreamRequestResponse>send(buildStreamRequest(partition)))
                        .map(response -> response.stream())
        );
    }

    public StreamRequestRequest buildStreamRequest(Integer partition){
        AbstractDCPFlowHandler.LastSnapshotReceived lastSnapshotReceived = _flowHandler.getLastSnapshot(_bucket, partition.shortValue());
        if(lastSnapshotReceived!=null){
            return new StreamRequestRequest(partition.shortValue(),0,0, MAX_DCP_SEQUENCE,lastSnapshotReceived.getStartSequenceNumber(),lastSnapshotReceived.getEndSequenceNumber(),_bucket);
        }
        else {
            return new StreamRequestRequest(partition.shortValue(), _bucket);
        }
    }

    public Boolean stop(){
        return _core.send(new CloseBucketRequest(_bucket)).map(reponse->reponse.status().isSuccess()).toBlocking().first();
    }
}

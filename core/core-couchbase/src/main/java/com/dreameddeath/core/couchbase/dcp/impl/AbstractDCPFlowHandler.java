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

package com.dreameddeath.core.couchbase.dcp.impl;

import com.codahale.metrics.MetricRegistry;
import com.couchbase.client.core.message.dcp.MutationMessage;
import com.couchbase.client.core.message.dcp.RemoveMessage;
import com.couchbase.client.core.message.dcp.SnapshotMarkerMessage;
import com.couchbase.client.deps.com.lmax.disruptor.EventHandler;
import com.couchbase.client.deps.com.lmax.disruptor.ExceptionHandler;
import com.dreameddeath.core.couchbase.dcp.DCPEvent;
import com.dreameddeath.core.couchbase.dcp.exception.HandlerException;
import com.dreameddeath.core.couchbase.metrics.DcpFlowHandlerMetrics;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 29/05/2015.
 */
public abstract class AbstractDCPFlowHandler {
    private final Handler handler;
    private final MappingMode mappingMode;
    //private final Counter eventCounter;
    private final Map<Pattern,ITranscoder> keyPatternsMap=new ConcurrentHashMap<>();
    private final ITranscoder genericTranscoder;
    private final IDocumentInfoMapper documentInfoMapper;
    private final DcpFlowHandlerMetrics mutationMetrics;
    private final DcpFlowHandlerMetrics deletionMetrics;
    private final DcpFlowHandlerMetrics snapshotMetrics;

    public AbstractDCPFlowHandler(Builder builder) {
        Preconditions.checkNotNull(builder.handlerName,"An handler name must be given");
        Preconditions.checkNotNull(builder.mappingMode,"A mapping mode must be given");

        this.mappingMode = builder.mappingMode;
        switch(this.mappingMode){
            case DOCUMENT_MAPPER: Preconditions.checkNotNull(builder.documentInfoMapper,"A document Mapper must be given");break;
            case GENERIC_TRANSCODER: Preconditions.checkNotNull(builder.genericTranscoder,"A transcoder must be given");break;
            case KEY_PATTERN: Preconditions.checkNotNull(builder.keyPatternsMap,"A transcoder must be given");break;
        }
        addKeyPatternsMap(builder.keyPatternsMap);
        this.genericTranscoder = builder.genericTranscoder;
        this.documentInfoMapper = builder.documentInfoMapper;
        this.mutationMetrics = new DcpFlowHandlerMetrics("DcpFlowHandler=\""+builder.handlerName+"\", Event=\"Mutation\"", builder.registry);
        this.deletionMetrics = new DcpFlowHandlerMetrics("DcpFlowHandler=\""+builder.handlerName+"\", Event=\"Deletion\"", builder.registry);
        this.snapshotMetrics = new DcpFlowHandlerMetrics("DcpFlowHandler=\""+builder.handlerName+"\", Event=\"Snapshot\"", builder.registry);
        handler = new Handler();
    }

    public void addKeyPatternsMap(Map<String,ITranscoder> keyPatternMap){
        if(keyPatternMap==null) return;
        for(Map.Entry<String,ITranscoder> entry:keyPatternMap.entrySet()){
            this.addKeyPatternEntry(entry.getKey(),entry.getValue());
        }
    }

    public void addKeyPatternEntry(String pattern, ITranscoder transcoder){
        addKeyPatternEntry(Pattern.compile(pattern),transcoder);
    }

    public void addKeyPatternEntry(Pattern pattern, ITranscoder transcoder){
        if(mappingMode==MappingMode.KEY_PATTERN){
            keyPatternsMap.putIfAbsent(pattern,transcoder);
        }
        else{
            throw new IllegalArgumentException("Cannot add a pattern based mapping while being in mode "+mappingMode.toString());
        }
    }


    public EventHandler<DCPEvent> getEventHandler(){
        return handler;
    }

    public ExceptionHandler<DCPEvent> getExceptionHandler(){
        return handler;
    }

    public ITranscoder findTranscoder(MutationMessage message){
        if(mappingMode==MappingMode.KEY_PATTERN){
            for(Pattern pattern:keyPatternsMap.keySet()){
                if(pattern.matcher(message.key()).matches()){
                    return keyPatternsMap.get(pattern);
                }
            }
            return null;
        }
        else if(mappingMode==MappingMode.GENERIC_TRANSCODER){
            return genericTranscoder;
        }
        else if(mappingMode==MappingMode.DOCUMENT_MAPPER){
            try {
                return documentInfoMapper.getMappingFromKey(message.key()).classMappingInfo().getAttachedObject(ITranscoder.class);
            }
            catch(MappingNotFoundException e){
                return null;
            }
        }
        return null;
    }

    public abstract LastSnapshotReceived getLastSnapshot(String bucketName, short partition);
    public abstract void manageSnapshotMessage(SnapshotMessage message);
    public abstract void manageMutationMessage(MutationMessage message, CouchbaseDocument mappedObject);
    public abstract void manageDeletionMessage(RemoveMessage message);

    public abstract void manageException(HandlerException message);


    public static class SnapshotMessage{
        private final String bucketName;
        private final short partition;
        private final long sequenceNumber;

        public SnapshotMessage(String bucketName, short partition, long sequenceNumber) {
            this.bucketName = bucketName;
            this.partition = partition;
            this.sequenceNumber = sequenceNumber;
        }


        public SnapshotMessage(SnapshotMarkerMessage message) {
            this(message.bucket(),message.partition(),message.endSequenceNumber());
        }


        public String getBucketName() {
            return bucketName;
        }

        public short getPartition() {
            return partition;
        }

        public long getSequenceNumber() {
            return sequenceNumber;
        }
    }

    public static class LastSnapshotReceived{
        private final long sequenceNumber;

        public LastSnapshotReceived(long sequenceNumber){
            this.sequenceNumber = sequenceNumber;
        }

        public long getSequenceNumber() {
            return sequenceNumber;
        }
    }

    public enum MappingMode {
        KEY_PATTERN,
        GENERIC_TRANSCODER,
        DOCUMENT_MAPPER
    }

    public class Handler implements EventHandler<DCPEvent>,ExceptionHandler<DCPEvent>{
        @Override
        public void onEvent(DCPEvent event, long sequence, boolean endOfBatch) throws Exception{
            DcpFlowHandlerMetrics.Context metricContext=null;
            try {
                switch (event.getType()) {
                    case MUTATION:
                        MutationMessage message = event.asMutationMessage();
                        byte[] messageContent = message.content().array();
                        metricContext = mutationMetrics.start((long) messageContent.length);
                        ITranscoder transcoder = findTranscoder(message);
                        CouchbaseDocument doc = transcoder.decode(messageContent);
                        doc.getBaseMeta().setKey(message.key());
                        doc.getBaseMeta().setCas(message.cas());
                        doc.getBaseMeta().setEncodedFlags(message.flags());
                        doc.getBaseMeta().setDbData(messageContent);
                        manageMutationMessage(event.asMutationMessage(), doc);
                        break;
                    case DELETION:
                        metricContext = deletionMetrics.start();
                        manageDeletionMessage(event.asDeletionMessage());
                        break;
                    case SNAPSHOT:
                        metricContext = snapshotMetrics.start();
                        manageSnapshotMessage(new SnapshotMessage(event.asSnapshotMessage()));
                        break;
                    default:
                }
                if(metricContext!=null){
                    metricContext.stop();
                }
            }
            catch(Throwable e){
                if(metricContext!=null){
                    metricContext.stop(e);
                }
                throw e;
            }
        }

        @Override
        public void handleEventException(Throwable ex, long sequence, DCPEvent event){
            manageException(new HandlerException(ex,sequence,event));
        }

        @Override
        public void handleOnStartException(Throwable ex){
            manageException(new HandlerException(ex));
        }

        @Override
        public void handleOnShutdownException(Throwable ex){
            manageException(new HandlerException(ex));
        }
    }

    public abstract static class Builder<T extends Builder<T>>{
        private String handlerName=null;
        private MappingMode mappingMode=null;
        private MetricRegistry registry=null;
        private Map<String,ITranscoder> keyPatternsMap=null;
        private ITranscoder genericTranscoder=null;
        private IDocumentInfoMapper documentInfoMapper=null;

        public T withRegistry(MetricRegistry registry) {
            this.registry = registry;
            return (T)this;
        }

        public T withKeyPatternsMap(Map<String, ITranscoder> keyPatternsMap) {
            this.keyPatternsMap = keyPatternsMap;
            this.mappingMode = MappingMode.KEY_PATTERN;
            return (T)this;
        }

        public T withGenericTranscoder(ITranscoder genericTranscoder) {
            this.genericTranscoder = genericTranscoder;
            this.mappingMode = MappingMode.GENERIC_TRANSCODER;
            return (T)this;
        }

        public T withDocumentInfoMapper(IDocumentInfoMapper documentInfoMapper) {
            this.documentInfoMapper = documentInfoMapper;
            this.mappingMode = MappingMode.DOCUMENT_MAPPER;
            return (T)this;
        }

        public T withHandlerName(String handlerName) {
            this.handlerName = handlerName;
            return (T)this;
        }
    }
}

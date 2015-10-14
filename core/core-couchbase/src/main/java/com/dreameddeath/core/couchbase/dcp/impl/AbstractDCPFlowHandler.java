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

package com.dreameddeath.core.couchbase.dcp.impl;

import com.couchbase.client.core.message.dcp.MutationMessage;
import com.couchbase.client.core.message.dcp.RemoveMessage;
import com.couchbase.client.core.message.dcp.SnapshotMarkerMessage;
import com.couchbase.client.deps.com.lmax.disruptor.EventHandler;
import com.couchbase.client.deps.com.lmax.disruptor.ExceptionHandler;
import com.dreameddeath.core.couchbase.dcp.DCPEvent;
import com.dreameddeath.core.couchbase.dcp.exception.HandlerException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.transcoder.ITranscoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 29/05/2015.
 */
public abstract class AbstractDCPFlowHandler {
    private final Handler handler;
    private final MappingMode mappingMode;
    private Map<Pattern,ITranscoder<?>> keyPatternsMap = new ConcurrentHashMap<>();
    private ITranscoder<?> genericTranscoder=null;
    private IDocumentInfoMapper documentInfoMapper=null;

    public AbstractDCPFlowHandler(ITranscoder transcoder){
        mappingMode = MappingMode.GENERIC_TRANSCODER;
        genericTranscoder = transcoder;
        handler = new Handler();
    }

    public AbstractDCPFlowHandler(IDocumentInfoMapper mapper){
        mappingMode = MappingMode.DOCUMENT_MAPPER;
        documentInfoMapper = mapper;
        handler = new Handler();
    }

    public AbstractDCPFlowHandler(Map<String,ITranscoder> keyPatternMap){
        mappingMode = MappingMode.KEY_PATTERN;
        if(keyPatternMap!=null) {
            addKeyPatternsMap(keyPatternMap);
        }
        handler = new Handler();
    }

    /*public AbstractDCPFlowHandler(){
        this((Map<String,ITranscoder>) null);
    }*/

    public void addKeyPatternsMap(Map<String,ITranscoder> keyPatternMap){
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

    public ExceptionHandler getExceptionHandler(){
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
    public abstract void manageSnapshotMessage(SnapshotMarkerMessage message);
    public abstract void manageMutationMessage(MutationMessage message, CouchbaseDocument mappedObject);
    public abstract void manageDeletionMessage(RemoveMessage message);

    public abstract void manageException(HandlerException message);


    public static class LastSnapshotReceived{
        private final long startSequenceNumber;
        private final long endSequenceNumber;

        public LastSnapshotReceived(long startSequenceNumber,long endSequenceNumber){
            this.startSequenceNumber = startSequenceNumber;
            this.endSequenceNumber = endSequenceNumber;
        }

        public long getStartSequenceNumber() {
            return startSequenceNumber;
        }

        public long getEndSequenceNumber() {
            return endSequenceNumber;
        }
    }

    public enum MappingMode {
        KEY_PATTERN,
        GENERIC_TRANSCODER,
        DOCUMENT_MAPPER
    }

    public class Handler implements EventHandler<DCPEvent>,ExceptionHandler{
        @Override
        public void onEvent(DCPEvent event, long sequence, boolean endOfBatch) throws Exception{
            switch(event.getType()){
                case MUTATION:
                    MutationMessage message = event.asMutationMessage();
                    ITranscoder transcoder = findTranscoder(message);
                    byte[] messageContent = message.content().array();
                    CouchbaseDocument doc = transcoder.decode(messageContent);
                    doc.getBaseMeta().setKey(message.key());
                    doc.getBaseMeta().setCas(message.cas());
                    doc.getBaseMeta().setEncodedFlags(message.flags());
                    doc.getBaseMeta().setDbSize(messageContent.length);
                    manageMutationMessage(event.asMutationMessage(),doc);
                    break;
                case DELETION:
                    manageDeletionMessage(event.asDeletionMessage());
                    break;
                case SNAPSHOT:
                    manageSnapshotMessage(event.asSnapshotMessage());
                    break;
                default:
            }
        }

        @Override
        public void handleEventException(Throwable ex, long sequence, Object event){
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
}

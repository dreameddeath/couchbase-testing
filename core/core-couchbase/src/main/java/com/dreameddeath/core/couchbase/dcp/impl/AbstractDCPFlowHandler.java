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
    private final Handler _handler;
    private final MappingMode _mappingMode;
    private Map<Pattern,ITranscoder<?>> _keyPatternsMap = new ConcurrentHashMap<>();
    private ITranscoder<?> _genericTranscoder=null;
    private IDocumentInfoMapper _documentInfoMapper=null;

    public AbstractDCPFlowHandler(ITranscoder transcoder){
        _mappingMode = MappingMode.GENERIC_TRANSCODER;
        _genericTranscoder = transcoder;
        _handler = new Handler();
    }

    public AbstractDCPFlowHandler(IDocumentInfoMapper mapper){
        _mappingMode = MappingMode.DOCUMENT_MAPPER;
        _documentInfoMapper = mapper;
        _handler = new Handler();
    }

    public AbstractDCPFlowHandler(Map<String,ITranscoder> _keyPatternMap){
        _mappingMode = MappingMode.KEY_PATTERN;
        if(_keyPatternMap!=null) {
            addKeyPatternsMap(_keyPatternMap);
        }
        _handler = new Handler();
    }

    /*public AbstractDCPFlowHandler(){
        this((Map<String,ITranscoder>) null);
    }*/

    public void addKeyPatternsMap(Map<String,ITranscoder> _keyPatternMap){
        for(Map.Entry<String,ITranscoder> entry:_keyPatternMap.entrySet()){
            this.addKeyPatternEntry(entry.getKey(),entry.getValue());
        }
    }

    public void addKeyPatternEntry(String pattern, ITranscoder transcoder){
        addKeyPatternEntry(Pattern.compile(pattern),transcoder);
    }

    public void addKeyPatternEntry(Pattern pattern, ITranscoder transcoder){
        if(_mappingMode==MappingMode.KEY_PATTERN){
            _keyPatternsMap.putIfAbsent(pattern,transcoder);
        }
        else{
            throw new IllegalArgumentException("Cannot add a pattern based mapping while being in mode "+_mappingMode.toString());
        }
    }


    public EventHandler<DCPEvent> getEventHandler(){
        return _handler;
    }

    public ExceptionHandler getExceptionHandler(){
        return _handler;
    }

    public ITranscoder findTranscoder(MutationMessage message){
        if(_mappingMode==MappingMode.KEY_PATTERN){
            for(Pattern pattern:_keyPatternsMap.keySet()){
                if(pattern.matcher(message.key()).matches()){
                    return _keyPatternsMap.get(pattern);
                }
            }
            return null;
        }
        else if(_mappingMode==MappingMode.GENERIC_TRANSCODER){
            return _genericTranscoder;
        }
        else if(_mappingMode==MappingMode.DOCUMENT_MAPPER){
            try {
                return _documentInfoMapper.getMappingFromKey(message.key()).classMappingInfo().getAttachedObject(ITranscoder.class);
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
        private final long _startSequenceNumber;
        private final long _endSequenceNumber;

        public LastSnapshotReceived(long startSequenceNumber,long endSequenceNumber){
            _startSequenceNumber = startSequenceNumber;
            _endSequenceNumber = endSequenceNumber;
        }

        public long getStartSequenceNumber() {
            return _startSequenceNumber;
        }

        public long getEndSequenceNumber() {
            return _endSequenceNumber;
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

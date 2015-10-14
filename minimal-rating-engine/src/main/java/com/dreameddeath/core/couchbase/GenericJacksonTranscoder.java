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

package com.dreameddeath.core.couchbase;


import com.couchbase.client.core.lang.Tuple;
import com.couchbase.client.core.lang.Tuple2;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.dreameddeath.core.couchbase.exception.DocumentDecodingException;
import com.dreameddeath.core.couchbase.exception.DocumentEncodingException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.BucketDocument;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class GenericJacksonTranscoder<T extends BaseCouchbaseDocument> extends GenericTranscoder<T>{
    private final static Logger logger = LoggerFactory.getLogger(GenericJacksonTranscoder.class);

    private static final ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setAnnotationIntrospector(new CouchbaseDocumentIntrospector());
        mapper.registerModule(new JodaModule());
    }

    public GenericJacksonTranscoder(Class<T> clazz,Class<? extends BucketDocument<T>> baseDocumentClazz){
        super(clazz,baseDocumentClazz);
        try {
            //_mapper.getSerializerProvider().findTypedValueSerializer(clazz, true, null);
        }
        catch (Exception e){
            logger.error("Error during transcoder init for class <{}>",clazz.getName(),e);
            throw new RuntimeException("Error during transcoder init for class <"+clazz.getName()+">");
        }
    }

    @Override
    public BucketDocument<T> decode(String id, ByteBuf content, long cas, int expiry, int flags, ResponseStatus status) {
        try {
            T result = mapper.readValue(content.array(),getBaseClass());
            result.getBaseMeta().setDbSize(content.array().length);
            return newDocument(id,expiry,result,cas);
        }
        catch (JsonParseException e){
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonTranscoder<"+getBaseClass().getName()+"> :",content,e);
        }
        catch (JsonMappingException e) {
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonTranscoder<"+getBaseClass().getName()+"> :",content,e);
        }
        catch(IOException e){
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonTranscoder<"+getBaseClass().getName()+"> :",content,e);
        }
    }

    @Override
    public Tuple2<ByteBuf, Integer> encode(BucketDocument<T> document) {
        try {
            byte[] encoded = mapper.writeValueAsBytes(document.content());
            document.content().getBaseMeta().setDbSize(encoded.length);
            return Tuple.create(Unpooled.wrappedBuffer(encoded), document.content().getBaseMeta().getEncodedFlags());
        }
        catch (JsonProcessingException e){
            throw new DocumentEncodingException("Error during encoding of data using GenericJacksonTranscoder<"+getBaseClass().getName()+">  of input "+document.toString(),e);
        }
    }
}

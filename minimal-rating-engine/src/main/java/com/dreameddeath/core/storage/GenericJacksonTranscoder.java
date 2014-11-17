package com.dreameddeath.core.storage;


import com.couchbase.client.core.lang.Tuple;
import com.couchbase.client.core.lang.Tuple2;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.couchbase.client.java.transcoder.Transcoder;
import com.dreameddeath.core.exception.storage.DocumentDecodingException;
import com.dreameddeath.core.exception.storage.DocumentEncodingException;
import com.dreameddeath.core.exception.storage.DocumentSetUpException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import com.dreameddeath.core.model.common.BucketDocument;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class GenericJacksonTranscoder<T extends BaseCouchbaseDocument> extends GenericTranscoder<T>{
    private final static Logger logger = LoggerFactory.getLogger(GenericJacksonTranscoder.class);

    private static final ObjectMapper _mapper;
    static {
        _mapper = new ObjectMapper();
        _mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        _mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        _mapper.setAnnotationIntrospector(new CouchbaseDocumentIntrospector());
        _mapper.registerModule(new JodaModule());
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
            T result = _mapper.readValue(content.array(),getBaseClass());
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
            byte[] encoded = _mapper.writeValueAsBytes(document.content());
            document.content().getBaseMeta().setDbSize(encoded.length);
            return Tuple.create(Unpooled.wrappedBuffer(encoded), document.content().getBaseMeta().getEncodedFlags());
        }
        catch (JsonProcessingException e){
            throw new DocumentEncodingException("Error during encoding of data using GenericJacksonTranscoder<"+getBaseClass().getName()+">  of input "+document.toString(),e);
        }
    }
}
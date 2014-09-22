package com.dreameddeath.core.storage;


import com.dreameddeath.core.exception.storage.DocumentDecodingException;
import com.dreameddeath.core.exception.storage.DocumentEncodingException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class GenericJacksonTranscoder<T extends BaseCouchbaseDocument> implements Transcoder<T>{
    private final static Logger logger = LoggerFactory.getLogger(GenericJacksonTranscoder.class);

    private static final ObjectMapper _mapper;
    private final Class<T> _dummyClass;
    
    static {
        _mapper = new ObjectMapper();
        _mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        _mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        _mapper.setAnnotationIntrospector(new CouchbaseDocumentIntrospector());
        _mapper.registerModule(new JodaModule());
    }
    
    
    public GenericJacksonTranscoder(Class<T> clazz){
        super();
        _dummyClass=clazz;
        try {
            _mapper.getSerializerProvider().findTypedValueSerializer(clazz, true, null);
        }
        catch (Exception e){

        }
    }
    
    @Override
    public int getMaxSize(){
        return CachedData.MAX_SIZE;
    }
    
    @Override
    public boolean asyncDecode(CachedData cachedData){
        return false;
    }
    
    
    @Override
    public T decode(CachedData cachedData){
        try{
            T result = _mapper.readValue(cachedData.getData(),_dummyClass);
            result.setDocumentDbSize(cachedData.getData().length);
            return result;
        }
        catch (JsonParseException e){
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonTranscoder<"+_dummyClass.getName()+"> :",cachedData,e);
        }
        catch (JsonMappingException e) {
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonTranscoder<"+_dummyClass.getName()+"> :",cachedData,e);
        }
        catch(IOException e){
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonTranscoder<"+_dummyClass.getName()+"> :",cachedData,e);
        }
    }
    
    @Override
    public CachedData encode(T input){
        try{
            byte[] encoded= _mapper.writeValueAsBytes(input);
            input.setDocumentDbSize(encoded.length);
            return new CachedData(0,encoded,CachedData.MAX_SIZE);
        }
        catch (JsonProcessingException e){
            throw new DocumentEncodingException("Error during encoding of data using GenericJacksonTranscoder<"+_dummyClass.getName()+">  of input "+input.toString(),e);
        }
    }
}

package com.dreameddeath.common.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.CachedData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.dreameddeath.common.storage.CouchbaseConstants;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import com.dreameddeath.common.model.CouchbaseDocument;


public class GenericJacksonTranscoder<T extends CouchbaseDocument> implements Transcoder<T>{
    private static final ObjectMapper _mapper;
    private final Class<T> _dummyClass;
    
    static {
        _mapper = new ObjectMapper();
        _mapper.registerModule(new JodaModule());
    }
    
    
    public GenericJacksonTranscoder(Class<T> clazz){
        super();
        _dummyClass=clazz;
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
            T result = (T)_mapper.readValue(cachedData.getData(),_dummyClass);
            result.setDbDocSize(cachedData.getData().length);
            return result;
        }
        catch (JsonParseException e){
            e.printStackTrace();
        }
        catch (JsonMappingException e) {
            e.printStackTrace();
        } 
        catch (JsonProcessingException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public CachedData encode(T input){
        try{
            byte[] encoded= _mapper.writeValueAsBytes(input);
            input.setDbDocSize(encoded.length);
            return new CachedData(0,encoded,CachedData.MAX_SIZE);
        }
        catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return null;
    }
       
}

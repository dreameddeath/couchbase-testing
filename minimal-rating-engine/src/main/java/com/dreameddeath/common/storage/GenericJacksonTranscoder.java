package com.dreameddeath.rating.storage;

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
import com.dreameddeath.rating.model.context.*; 

/**
*  Class used to perform storage 
*/
public class GenericTranscoder<T> implements Transcoder<T>{
    private static final ObjectMapper _mapper = new ObjectMapper();
    private final Class<T> _dummyClass;
    
    public RatingContextTranscoder(Class<T> clazz){
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
            return (T)_mapper.readValue(cachedData.getData(),_dummyClass);
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
            return new CachedData(0,_mapper.writeValueAsBytes(input),CachedData.MAX_SIZE);
        }
        catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return null;
    }
       
}

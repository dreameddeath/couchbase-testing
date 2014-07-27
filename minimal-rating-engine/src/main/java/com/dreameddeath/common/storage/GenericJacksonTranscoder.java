package com.dreameddeath.common.storage;


import com.dreameddeath.common.model.document.CouchbaseDocument;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

import java.io.IOException;


public class GenericJacksonTranscoder<T extends CouchbaseDocument> implements Transcoder<T>{
    private static final ObjectMapper _mapper;
    private final Class<T> _dummyClass;
    
    static {
        _mapper = new ObjectMapper();
        _mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        _mapper.setAnnotationIntrospector(new CouchbaseDocumentIntrospector());
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
        String baseString="";
        try{
            baseString=new String(cachedData.getData(),"UTF-8");
            T result = _mapper.readValue(cachedData.getData(),_dummyClass);
            result.setDbDocSize(cachedData.getData().length);
            return result;
        }
        catch (JsonParseException e){
            System.out.println("Raw data <" +baseString+">");
            e.printStackTrace();
        }
        catch (JsonMappingException e) {
            System.out.println("Raw data <" +baseString+">");
            e.printStackTrace();
        } 
        catch (IOException e) {
            System.out.println("Raw data <" +baseString+">");
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

package com.dreameddeath.core.transcoder.json;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.transcoder.JacksonTransformers;
import com.dreameddeath.core.exception.view.ViewDecodingException;
import com.dreameddeath.core.exception.view.ViewEncodingException;
import com.dreameddeath.core.model.view.IViewTranscoder;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

/**
 * Created by ceaj8230 on 22/12/2014.
 */
public abstract class ViewJsonObjectTranscoder<T> implements IViewTranscoder<T> {
    private final Class _rootClass;
    protected abstract Class<T> getBaseClass();

    protected Class getRootClass(){ return _rootClass;}
    public ViewJsonObjectTranscoder(){
        _rootClass = GenericJacksonTranscoder.findRootClass(getBaseClass());
    }

    @Override
    public T decode(Object value) throws ViewDecodingException {
        byte[] couchbaseEncodingResult;
        try {
            couchbaseEncodingResult = JacksonTransformers.MAPPER.writeValueAsBytes((JsonObject)value);
        }
        catch(com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException e){
            throw new ViewDecodingException(value,"Error during encoding of data using Couchbase Transcoder<" + getBaseClass().getName() + "> :", e);
        }

        try{
            return (T)GenericJacksonTranscoder.MAPPER.readValue(couchbaseEncodingResult,getRootClass());
        }
        catch (IOException e){
            throw new ViewDecodingException(value,"Error during decoding of data using Generic Transcoder for class<" + getBaseClass().getName() + "> :"+ couchbaseEncodingResult, e);
        }
    }

    @Override
    public Object encode(T value) throws ViewEncodingException {
        byte[] genericEncodingResult;
        try {
            genericEncodingResult = GenericJacksonTranscoder.MAPPER.writeValueAsBytes(value);
        }
        catch (JsonProcessingException e) {
            throw new ViewEncodingException(value,"Error during encoding of data using Generic Transcoder<" + getBaseClass().getName() + "> :", e);
        }

        try{
            return JacksonTransformers.MAPPER.readValue(genericEncodingResult, JsonObject.class);
        }
        catch(IOException e){
            throw new ViewEncodingException(value,"Error during encoding of data using GenericJacksonCouchbaseTranscoder<"+getBaseClass().getName()+">",e);
        }
    }
}

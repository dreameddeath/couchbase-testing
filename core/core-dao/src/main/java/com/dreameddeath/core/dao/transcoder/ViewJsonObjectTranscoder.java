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

package com.dreameddeath.core.dao.transcoder;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.transcoder.JacksonTransformers;
import com.dreameddeath.core.dao.exception.view.ViewDecodingException;
import com.dreameddeath.core.dao.exception.view.ViewEncodingException;
import com.dreameddeath.core.dao.model.view.IViewTranscoder;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentObjectMapperConfigurator;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 22/12/2014.
 */
public abstract class ViewJsonObjectTranscoder<T> implements IViewTranscoder<T> {
    private final Class rootClass;
    private final ObjectMapper mapper= ObjectMapperFactory.BASE_INSTANCE.getMapper(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_STORAGE);
    protected abstract Class<T> getBaseClass();

    protected Class getRootClass(){ return rootClass;}
    public ViewJsonObjectTranscoder(){
        rootClass = GenericJacksonTranscoder.findRootClass(getBaseClass());
    }

    @Override
    public T decode(Object value) throws ViewDecodingException {
        byte[] couchbaseEncodingResult;
        try {
            couchbaseEncodingResult = JacksonTransformers.MAPPER.writeValueAsBytes(value);
        }
        catch(com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException e){
            throw new ViewDecodingException(value,"Error during encoding of data using Couchbase Transcoder<" + getBaseClass().getName() + "> :", e);
        }

        try{
            return (T)mapper.readValue(couchbaseEncodingResult,getRootClass());
        }
        catch (IOException e){
            throw new ViewDecodingException(value,"Error during decoding of data using Generic Transcoder for class<" + getBaseClass().getName() + "> :"+ couchbaseEncodingResult, e);
        }
    }

    @Override
    public Object encode(T value) throws ViewEncodingException {
        byte[] genericEncodingResult;
        try {
            genericEncodingResult = mapper.writeValueAsBytes(value);
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

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

package com.dreameddeath.core.transcoder.json;


import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.IVersionedDocument;
import com.dreameddeath.core.model.exception.transcoder.DocumentDecodingException;
import com.dreameddeath.core.model.exception.transcoder.DocumentEncodingException;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;


public class GenericJacksonTranscoder<T extends CouchbaseDocument> implements ITranscoder<T> {
    private final static Logger logger = LoggerFactory.getLogger(GenericJacksonTranscoder.class);

    public static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();
        MAPPER.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.setAnnotationIntrospector(new CouchbaseDocumentIntrospector());
        MAPPER.registerModule(new JodaModule());
        MAPPER.registerModule(new SimpleModule() {
            protected CouchbaseBusinessDocumentDeserializerModifier modifier = new CouchbaseBusinessDocumentDeserializerModifier();

            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                if (modifier != null) {
                    context.addBeanDeserializerModifier(modifier);
                }
            }
        });

    }


    private final Class<T> _dummyClass;
    private final Class _rootClass;

    public static Class findRootClass(Class clazz) {
        Class currentClass = clazz;
        //For versionned document, find the root class
        if (IVersionedDocument.class.isAssignableFrom(currentClass)) {
            JsonTypeIdResolver foundAnnot = null;
            while (!currentClass.isPrimitive()) {
                Annotation[] annot = currentClass.getDeclaredAnnotations();

                for (int pos = 0; pos < annot.length; ++pos) {
                    if (JsonTypeIdResolver.class.isAssignableFrom(annot[pos].getClass())) {
                        if (CouchbaseDocumentTypeIdResolver.class.isAssignableFrom(((JsonTypeIdResolver) annot[pos]).value())) {
                            foundAnnot = (JsonTypeIdResolver) annot[pos];
                            break;
                        }
                    }
                }

                if (foundAnnot != null) {
                    break;
                }
                currentClass = currentClass.getSuperclass();
            }
        }
        return currentClass;
    }

    public GenericJacksonTranscoder(Class<T> clazz){
        _dummyClass = clazz;
        _rootClass = findRootClass(clazz);


    }

    @Override
    public Class<T> getBaseClass() {return _dummyClass;}

    public Class getRootClass(){return _rootClass;}
    @Override
    public T decode(byte[] content) throws DocumentDecodingException{
        try {
            T result = (T) MAPPER.readValue(content, getRootClass());
            result.getBaseMeta().setDbSize(content.length);
            return result;
        }
        catch (IOException e) {
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonCouchbaseTranscoder<" + getBaseClass().getName() + "> :", content, e);
        }
    }



    @Override
    public byte[] encode(T doc) throws DocumentEncodingException{
        try {
            return MAPPER.writeValueAsBytes(doc);
        }
        catch (JsonProcessingException e){
            throw new DocumentEncodingException(doc,"Error during encoding of data using GenericJacksonCouchbaseTranscoder<"+getBaseClass().getName()+">",e);
        }
    }


}

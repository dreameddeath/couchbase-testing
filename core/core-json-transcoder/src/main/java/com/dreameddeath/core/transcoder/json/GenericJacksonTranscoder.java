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


import com.dreameddeath.core.json.IObjectMapperConfigurator;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.exception.transcoder.DocumentDecodingException;
import com.dreameddeath.core.model.exception.transcoder.DocumentEncodingException;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;


public class GenericJacksonTranscoder<T extends CouchbaseDocument> implements ITranscoder<T> {
    private final static Logger LOG = LoggerFactory.getLogger(GenericJacksonTranscoder.class);

    public enum Flavor{
        STORAGE(CouchbaseDocumentConfigurator.BASE_COUCHBASE_STORAGE),
        PUBLIC(CouchbaseDocumentConfigurator.BASE_COUCHBASE_PUBLIC),
        PRIVATE(CouchbaseDocumentConfigurator.BASE_COUCHBASE_INTERNAL);

        private IObjectMapperConfigurator.ConfiguratorType type;

        Flavor(IObjectMapperConfigurator.ConfiguratorType type){
            this.type=type;
        }

    }

    private final Class<T> dummyClass;
    private final Class rootClass;
    private final ObjectMapper mapper;



    public static Class findRootClass(Class clazz) {
        Class currentClass = clazz;
        //For versionned document, find the root class
        if (IVersionedEntity.class.isAssignableFrom(currentClass)) {
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

    public GenericJacksonTranscoder(Flavor flavor,Class<T> clazz){
        mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper(flavor.type);
        dummyClass = clazz;
        rootClass = findRootClass(clazz);
    }

    @Override
    public Class<T> getBaseClass() {return dummyClass;}

    public Class getRootClass(){return rootClass;}
    @Override
    public T decode(byte[] content) throws DocumentDecodingException{
        try {
            T result = (T) mapper.readValue(content, getRootClass());
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
            return mapper.writeValueAsBytes(doc);
        }
        catch (JsonProcessingException e){
            throw new DocumentEncodingException(doc,"Error during encoding of data using GenericJacksonCouchbaseTranscoder<"+getBaseClass().getName()+">",e);
        }
    }


}

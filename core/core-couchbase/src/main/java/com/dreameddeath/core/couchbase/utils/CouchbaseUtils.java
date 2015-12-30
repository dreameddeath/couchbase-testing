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

package com.dreameddeath.core.couchbase.utils;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.couchbase.config.CouchbaseConfigProperties;
import com.dreameddeath.core.couchbase.exception.TranscoderNotFoundException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Christophe Jeunesse on 20/10/2015.
 */
public class CouchbaseUtils {
    public static String ROOT_FILENAME_CLASS = "META-INF/core-couchbase/bucketDocument/";

    public static String getTargetBucketDocumentRegisteringFilename(BucketDocumentForClass annot){
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfoFromAnnot(annot,BucketDocumentForClass::value);
        return ROOT_FILENAME_CLASS+classInfo.getFullName();
    }

    public static class ApplicableBucketDocumentInfo {
        private final ClassInfo bucketDocumentClass;
        private final CouchbaseDocumentReflection effectiveCouchbaseDocumentclass;
        private ApplicableBucketDocumentInfo(ClassInfo bucketDocumentClass,CouchbaseDocumentReflection effectiveCouchbaseDocumentclass){
            this.bucketDocumentClass = bucketDocumentClass;
            this.effectiveCouchbaseDocumentclass = effectiveCouchbaseDocumentclass;
        }

        public ClassInfo getBucketDocumentClass() {
            return bucketDocumentClass;
        }

        public CouchbaseDocumentReflection getEffectiveCouchbaseDocumentclass() {
            return effectiveCouchbaseDocumentclass;
        }
    }


    public static ApplicableBucketDocumentInfo getBucketDocumentFromClass(CouchbaseDocumentReflection docReflexion){
        String filename = ROOT_FILENAME_CLASS+docReflexion.getClassInfo().getFullName();
        InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        if(is==null){
            if(docReflexion.getSuperclassReflection()!=null){
                return getBucketDocumentFromClass(docReflexion.getSuperclassReflection());
            }
            else{
                return null;
            }
        }
        else {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
            try {
                String className = fileReader.readLine();
                return new ApplicableBucketDocumentInfo((ClassInfo) AbstractClassInfo.getClassInfo(className),docReflexion);
            }
            catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException("Cannot find/read file <" + filename + "> for clazz <" + docReflexion.getClassInfo().getCompiledFileName() + ">", e);
            }
        }
    }

    public static <T extends CouchbaseDocument> ApplicableBucketDocumentInfo resolveBucketDocumentForClass(Class<T> entityClass) throws ClassNotFoundException{
        CouchbaseDocumentReflection entityDocInfo = CouchbaseDocumentReflection.getReflectionFromClass(entityClass);
        EntityModelId modelId = entityDocInfo.getStructure().getEntityModelId();

        String bucketDocClassName = CouchbaseConfigProperties.COUCHBASE_BUCKETDOCUMENT_CLASS_NAME.getProperty(modelId.getDomain(), modelId.getName()).get();
        if(bucketDocClassName==null){
            return CouchbaseUtils.getBucketDocumentFromClass(entityDocInfo);
        }
        else{
            return new ApplicableBucketDocumentInfo((ClassInfo)AbstractClassInfo.getClassInfo(bucketDocClassName),entityDocInfo) ;
        }
    }

    public static <T extends CouchbaseDocument> ITranscoder<T> resolveTranscoderForClass(Class<T> entityClass) throws TranscoderNotFoundException{
        try {
            CouchbaseDocumentReflection entityDocInfo = CouchbaseDocumentReflection.getReflectionFromClass(entityClass);
            EntityModelId modelId = entityDocInfo.getStructure().getEntityModelId();
            String transcoderClassName = CouchbaseConfigProperties.COUCHBASE_TRANSCODER_CLASS_NAME.getProperty(modelId.getDomain(), modelId.getName()).get();
            if (transcoderClassName == null) {
                return new GenericJacksonTranscoder<>(GenericJacksonTranscoder.Flavor.STORAGE,(Class<T>)entityDocInfo.getClassInfo().getCurrentClass());
            }
            else {
                Class<? extends ITranscoder<T>> transcoderClass = (Class<? extends ITranscoder<T>>) Thread.currentThread().getContextClassLoader().loadClass(transcoderClassName);
                return transcoderClass.getConstructor(Class.class).newInstance(entityDocInfo.getClassInfo().getCurrentClass());
            }
        }
        catch(NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException|ClassNotFoundException e){
            throw new TranscoderNotFoundException(entityClass,e);
        }
    }
}

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

package com.dreameddeath.core.model.entity;

import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 13/10/2015.
 */
public class EntityDefinitionManager {
    private final static Logger LOG = LoggerFactory.getLogger(EntityDefinitionManager.class);

    public static final String ROOT_PATH="META-INF/core-model";
    public static final String DOCUMENT_DEF_PATH="DocumentDef";

    public final static ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.setTimeZone(TimeZone.getDefault());
        //MAPPER.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }

    private Map<String,Class> versionClassMap=new ConcurrentHashMap<>();

    public EntityDefinitionManager(){}

    public void buildEntityDefinitionFile(Writer writer,CouchbaseDocumentReflection documentDef) throws IOException{
        MAPPER.writeValue(writer,EntityDef.build(documentDef.getStructure()));
    }

    public String getDocumentEntityFilename(EntityModelId modelId){
        return String.format("%s/%s/%s/%s/v%s.json", ROOT_PATH, DOCUMENT_DEF_PATH,
                modelId.getDomain(),
                modelId.getName(),
                modelId.getEntityVersion().getMajor());
    }

    public Class findClassFromVersionnedTypeId(EntityModelId modelId){
        Class result =versionClassMap.get(modelId.getClassUnivoqueModelId());
        if(result==null) {
            //it will naturally exclude patch from typeId
            String filename = getDocumentEntityFilename(modelId);
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (is == null) {
                throw new RuntimeException("Cannot find/read file <" + filename + "> for id <" + modelId.toString() + ">");
            }
            try {
                EntityDef def = MAPPER.readValue(is, EntityDef.class);
                result = Thread.currentThread().getContextClassLoader().loadClass(def.getClassName());
                versionClassMap.putIfAbsent(modelId.getClassUnivoqueModelId(), result);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException("Cannot find/read file <" + filename + "> for id <" + modelId.toString() + ">", e);
            }
        }
        return result;
    }

    public Class findClassFromVersionnedTypeId(String typeId){
        return findClassFromVersionnedTypeId(EntityModelId.build(typeId));
    }


    public synchronized List<EntityDef> getEntities(){
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resultResources= resolver.getResources("classpath*:" + ROOT_PATH + "/" + DOCUMENT_DEF_PATH + "/*/*/*.json");
            List<EntityDef> result = new ArrayList<>(resultResources.length);
            for(Resource entityResource:resultResources){
                try {
                    result.add(MAPPER.readValue(entityResource.getInputStream(), EntityDef.class));
                }
                catch(Throwable e){
                    LOG.error("Cannot read entity file <"+entityResource.getFile().getAbsolutePath()+">",e);
                    throw new RuntimeException("Cannot read entity file <"+entityResource.getFilename()+">",e);
                }
            }
            return result;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}

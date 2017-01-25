/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.model.entity;

import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.annotation.HasDomainsOfSubClass;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 13/10/2015.
 */
public class EntityDefinitionManager {
    private final static Logger LOG = LoggerFactory.getLogger(EntityDefinitionManager.class);

    public static final String ROOT_PATH="META-INF/core-model";
    public static final String DOCUMENT_DEF_PATH="DocumentDef";

    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();
    private final Map<String,Class> versionClassMap=new ConcurrentHashMap<>();
    private final AtomicReference<List<EntityDef>> cachedEntities=new AtomicReference<>();

    public EntityDefinitionManager(){}

    public void buildEntityDefinitionFile(Writer writer,CouchbaseDocumentReflection documentDef) throws IOException{
        mapper.writeValue(writer,EntityDef.build(documentDef.getStructure()));
    }

    public String getDocumentEntityFilename(EntityModelId modelId){
        return String.format("%s/%s/%s/%s/v%s.json", ROOT_PATH, DOCUMENT_DEF_PATH,
                modelId.getDomain().toLowerCase(),
                modelId.getName().toLowerCase(),
                modelId.getEntityVersion().getMajor());
    }

    public Class findClassFromVersionnedTypeId(EntityModelId modelId){
        return versionClassMap.computeIfAbsent(modelId.getClassUnivoqueModelId(),model->{
            //it will naturally exclude patch from typeId
            String filename = getDocumentEntityFilename(modelId);
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (is == null) {
                throw new RuntimeException("Cannot find/read file <" + filename + "> for id <" + modelId.toString() + ">");
            }
            try {
                EntityDef def = mapper.readValue(is, EntityDef.class);
                return Thread.currentThread().getContextClassLoader().loadClass(def.getClassName());
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException("Cannot find/read file <" + filename + "> for id <" + modelId.toString() + ">", e);
            }
        });

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
                    result.add(mapper.readValue(entityResource.getInputStream(), EntityDef.class));
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

    public List<EntityDef> getCachedEntities(){
        return cachedEntities.updateAndGet(list-> {
            if (list == null) {
                return getEntities();
            }
            else{
                return list;
            }
        });
    }

    public List<EntityDef> getChildEntities(final EntityDef parentEntity){
        return this.getCachedEntities()
                .stream().filter(entity -> entity.getParentEntities().contains(parentEntity.getModelId()))
                .collect(Collectors.toList());
    }


    public Set<String> getEffectiveDomains(final EntityDef rootEntity){
        List<EntityDef> childEntities = this.getChildEntities(rootEntity);
        Set<String> childDomains = childEntities.stream()
                .filter(entityDef -> !Modifier.isAbstract(findClassFromVersionnedTypeId(entityDef.getModelId()).getModifiers()))
                .filter(entityDef -> findClassFromVersionnedTypeId(entityDef.getModelId()).getAnnotation(HasDomainsOfSubClass.class)==null)
                .map(entityDef -> entityDef.getModelId().getDomain())
                .collect(Collectors.toSet());

        Set<String> subClassDomains = childEntities.stream()
                .map(entityDef -> (Class<?>)findClassFromVersionnedTypeId(entityDef.getModelId()))
                .filter(clazz -> clazz.getAnnotation(HasDomainsOfSubClass.class)!=null)
                .map(clazz -> clazz.getAnnotation(HasDomainsOfSubClass.class).value())
                .map(EntityDef::build)
                .flatMap(entityDef -> getChildEntities(entityDef).stream())
                .flatMap(entityDef -> getEffectiveDomains(entityDef).stream())
                .collect(Collectors.toSet());

        Set<String> result = new HashSet<>(childDomains);
        result.addAll(subClassDomains);
        if(!Modifier.isAbstract(findClassFromVersionnedTypeId(rootEntity.getModelId()).getModifiers())){
            result.add(rootEntity.getModelId().getDomain());
        }
        return result;
    }
}

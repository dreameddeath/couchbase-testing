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

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 13/10/2015.
 */
public class EntityDefinitionManager {
    public static final String ROOT_PATH="META-INF/core-annotation";
    public static final String DOCUMENT_DEF_PATH="DocumentDef";

    private final static EntityDefinitionManager ENTITY_DEFINITION_MANAGER_REF;
    static{
        ENTITY_DEFINITION_MANAGER_REF = new EntityDefinitionManager();
        ENTITY_DEFINITION_MANAGER_REF.preloadClasses();
    }

    public static EntityDefinitionManager getInstance(){
        return ENTITY_DEFINITION_MANAGER_REF;
    }

    private Map<String,Class> versionClassMap=new HashMap<>();

    private EntityDefinitionManager(){}

    public void buildEntityDefinitionFile(Writer writer,CouchbaseDocumentReflection documentDef){
        DocumentDef docDef = documentDef.getClassInfo().getAnnotation(DocumentDef.class);
        EntityModelId modelId = EntityModelId.build(docDef,documentDef.getClassInfo().getTypeElement());
        //TODO generate json
    }

    public String getDocumentEntityFilename(EntityModelId modelId){
        return String.format("%s/%s/%s.%s/v%s", ROOT_PATH, DOCUMENT_DEF_PATH,
                modelId.getDomain(),
                modelId.getName(),
                modelId.getEntityVersion().getMajor());
    }

    public Class findClassFromVersionnedTypeId(EntityModelId modelId){
        Class result =versionClassMap.get(modelId.getClassUnivoqueModelId());
        if(result==null) {
            //it will naturally exclude patch from typeId
            String filename = EntityDefinitionManager.getInstance().getDocumentEntityFilename(modelId);
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (is == null) {
                throw new RuntimeException("Cannot find/read file <" + filename + "> for id <" + modelId.toString() + ">");
            }
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
            try {
                String className = fileReader.readLine();
                result = Thread.currentThread().getContextClassLoader().loadClass(className);
                versionClassMap.put(modelId.getClassUnivoqueModelId(), result);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException("Cannot find/read file <" + filename + "> for id <" + modelId.toString() + ">", e);
            }
        }
        return result;
    }

    public Class findClassFromVersionnedTypeId(String typeId){
        return findClassFromVersionnedTypeId(EntityModelId.build(typeId));
    }


    private void preloadClasses(){
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            //Resource[] resultResources= resolver.getResources("classpath:" + ROOT_PATH + "/" + DOCUMENT_DEF_PATH + "/*/*");

        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

    }
}

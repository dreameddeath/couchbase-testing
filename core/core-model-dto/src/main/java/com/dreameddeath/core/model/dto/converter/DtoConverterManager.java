/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.model.dto.converter;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.dto.annotation.DtoConverterForEntity;
import com.dreameddeath.core.model.dto.converter.model.DtoConverterDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by christophe jeunesse on 07/03/2017.
 */
public class DtoConverterManager {
    public static final String ROOT_PATH="META-INF/core-model-dto";
    public static final String CONVERTER_DEF_PATH="converter_defs";
    private static final Logger LOG = LoggerFactory.getLogger(DtoConverterManager.class);

    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();
    private final AtomicReference<List<DtoConverterDef>> cachedConverterDef=new AtomicReference<>();
    private ClassLoader classLoader=null;

    public String getConverterDefPath(DtoConverterDef converterDef){
        return String.format("%s/%s/%s/%s.json",
                ROOT_PATH,
                CONVERTER_DEF_PATH,
                StringUtils.isEmpty(converterDef.getType())?"__UNKNOWN__":converterDef.getType(),
                converterDef.getConverterClass());
    }

    public DtoConverterDef buildConverterDefFromConverterClass(ClassInfo converterClassInfo){
        DtoConverterForEntity converterForEntityAnnot = converterClassInfo.getAnnotation(DtoConverterForEntity.class);
        CouchbaseDocumentStructureReflection converterTarget = CouchbaseDocumentStructureReflection.getClassInfoFromAnnot(converterForEntityAnnot, DtoConverterForEntity::entityClass);
        Preconditions.checkNotNull(converterTarget,"No target found for annot "+converterForEntityAnnot.toString());
        DtoConverterDef converterDef = new DtoConverterDef();
        converterDef.setConverterClass(converterClassInfo.getFullName());
        converterDef.setEntityModelId(converterTarget.getEntityModelId());
        converterDef.setEntityClassName(converterTarget.getClassInfo().getFullName());
        converterDef.setConverterVersion(converterForEntityAnnot.version());
        converterDef.setMode(converterForEntityAnnot.mode());
        converterDef.setType(converterForEntityAnnot.type());
        if(converterClassInfo.isInstanceOf(IDtoInputMapper.class)){
            AbstractClassInfo effectiveInputClass = AbstractClassInfo.getEffectiveGenericType(converterClassInfo, AbstractClassInfo.getClassInfo(IDtoInputMapper.class), 1);
            converterDef.setInputClass(effectiveInputClass.getFullName());
            converterDef.setInputVersion(converterForEntityAnnot.version());
        }
        if(converterClassInfo.isInstanceOf(IDtoOutputMapper.class)){
            AbstractClassInfo effectiveOutputClass = AbstractClassInfo.getEffectiveGenericType(converterClassInfo, AbstractClassInfo.getClassInfo(IDtoOutputMapper.class), 1);
            converterDef.setOutputClass(effectiveOutputClass.getFullName());
            converterDef.setOutputVersion(converterForEntityAnnot.version());
        }
        return converterDef;
    }

    public String getConverterDefPath(ClassInfo converterClass){
        DtoConverterDef converterDef = buildConverterDefFromConverterClass(converterClass);
        return getConverterDefPath(converterDef);
    }


    public DtoConverterManager(){
    }

    public DtoConverterManager(ClassLoader loader){
        this.classLoader = loader;
    }

    public void buildConverterDefFile(Writer writer, ClassInfo converterClassInfo) throws IOException {
        DtoConverterDef converterDef = buildConverterDefFromConverterClass(converterClassInfo);
        mapper.writeValue(writer,converterDef);
    }


    public synchronized List<DtoConverterDef> getConverterDefs(){
        try {
            PathMatchingResourcePatternResolver resolver = classLoader!=null?new PathMatchingResourcePatternResolver(classLoader):new PathMatchingResourcePatternResolver();
            Resource[] resultResources= resolver.getResources("classpath*:" + ROOT_PATH + "/" + CONVERTER_DEF_PATH + "/*/*.json");
            List<DtoConverterDef> result = new ArrayList<>(resultResources.length);
            for(Resource entityResource:resultResources){
                try {
                    result.add(mapper.readValue(entityResource.getInputStream(), DtoConverterDef.class));
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

    public List<DtoConverterDef> getCachedConvertersDef(){
        return cachedConverterDef.updateAndGet(list-> {
            if (list == null) {
                return getConverterDefs();
            }
            else{
                return list;
            }
        });
    }


    public List<DtoConverterDef> getConverterDefsForEntities(EntityModelId modelId){
        return getCachedConvertersDef().stream()
                .filter(converterDef->converterDef.getEntityModelId().equals(modelId))
                .collect(Collectors.toList());
    }

}

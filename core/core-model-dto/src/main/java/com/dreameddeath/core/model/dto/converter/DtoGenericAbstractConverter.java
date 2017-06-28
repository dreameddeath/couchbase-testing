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
import com.dreameddeath.core.model.dto.converter.model.DtoConverterDef;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by christophe jeunesse on 22/03/2017.
 */
public abstract class DtoGenericAbstractConverter<TCONV,TDOC, TTYPE> implements IDtoFactoryAware{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final List<EntityConverterForClass<TCONV,TDOC, TTYPE>> listConverters = new ArrayList<>();
    private final Class<TDOC> docClass;
    private final Class<TTYPE> typeClass;
    private final String version;

    public DtoGenericAbstractConverter(Class<TDOC> docClass, Class<TTYPE> typeClass,String version){
        this.docClass = docClass;
        this.typeClass = typeClass;
        this.version = version;
    }

    protected abstract TCONV getEffectiveConverter(DtoConverterFactory factory, DtoConverterDef dtoConverterDef, String version,EntityDef currEntityDef);

    private int getDeeperChildEntityFirst(EntityDef entityDefA, EntityDef entityDefB) {
        return entityDefB.getParentEntities().size()-entityDefA.getParentEntities().size();
    }

    private boolean isApplicableChildEntity(EntityDef childEntityDef){
        try{
            return !((ClassInfo) AbstractClassInfo.getClassInfo(childEntityDef.getClassName())).isAbstract();
        }
        catch (ClassNotFoundException e){
            throw new RuntimeException("Unexpected error",e);
        }
    }

    protected List<EntityConverterForClass<TCONV, TDOC, TTYPE>> getListConverters() {
        return listConverters;
    }

    @Override
    public void setDtoConverterFactory(DtoConverterFactory factory) {
        EntityDef rootEntityDef = EntityDef.build(CouchbaseDocumentStructureReflection.getReflectionFromClass(docClass));
        List<EntityDef> childEntities = factory.getEntityDefinitionManager().getChildEntities(rootEntityDef);
        List<EntityDef> notProcessedEntities = childEntities.stream().filter(this::isApplicableChildEntity).collect(Collectors.toList());
        notProcessedEntities.sort(this::getDeeperChildEntityFirst);
        for(EntityDef currEntityDef:notProcessedEntities) {
            List<DtoConverterDef> dtoConverterDefs = factory.getDtoConverterManager().getConverterDefsForEntities(currEntityDef.getModelId());
            for (DtoConverterDef converterDef : dtoConverterDefs) {
                TCONV effectiveConverter = getEffectiveConverter(factory,converterDef,version,currEntityDef);
                if(effectiveConverter!=null){
                    try{
                        @SuppressWarnings("unchecked")
                        Class<? extends TDOC> childDocClass=AbstractClassInfo.getClassInfo(currEntityDef.getClassName()).getCurrentClass();
                        listConverters.add(new EntityConverterForClass<>(childDocClass,typeClass,effectiveConverter));
                    }
                    catch(ClassNotFoundException e){
                        LOG.error("Error during init of converter ",e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    protected static class EntityConverterForClass<TCONV,TDOC,TTYPE>{
        private final Class<? extends TDOC> docClass;
        private final Class<? extends TTYPE> typeClass;
        private final TCONV converter;

        public EntityConverterForClass(Class<? extends TDOC> docClass,Class<? extends TTYPE> typeClass, TCONV converter) {
            this.docClass = docClass;
            this.typeClass = typeClass;
            this.converter = converter;
        }

        @SuppressWarnings("unchecked")
        public TCONV getConverter(){
            return converter;
        }

        public Class<? extends TDOC> getDocClass() {
            return docClass;
        }

        public Class<? extends TTYPE> getTypeClass() {
            return typeClass;
        }
    }
}

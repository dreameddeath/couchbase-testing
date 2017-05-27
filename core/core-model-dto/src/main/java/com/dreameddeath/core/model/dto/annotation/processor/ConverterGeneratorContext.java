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

package com.dreameddeath.core.model.dto.annotation.processor;

import com.dreameddeath.core.model.dto.annotation.processor.model.DtoModel;
import com.dreameddeath.core.model.dto.annotation.processor.model.EnumModel;
import com.dreameddeath.core.model.dto.converter.model.DtoConverterDef;
import com.dreameddeath.core.model.entity.model.EntityDef;

import java.util.*;

/**
 * Created by ceaj8230 on 09/03/2017.
 */
public class ConverterGeneratorContext {
    private final Map<String,DtoModel> models=new HashMap<>();
    private final Map<String,EnumModel> enums = new HashMap<>();
    private final Set<EntityDef> entityDefs;
    private final List<DtoConverterDef> converterDefs;

    public ConverterGeneratorContext(Set<EntityDef> entityDefs, List<DtoConverterDef> converterDefs) {
        this.entityDefs = new HashSet<>(entityDefs);
        this.converterDefs = new ArrayList<>(converterDefs);
    }

    public ConverterGeneratorContext() {
        this(Collections.emptySet(),Collections.emptyList());
    }


    public void addAllEntities(List<EntityDef> entities) {
        entityDefs.addAll(entities);
    }

    public void addAllConvertersDef(List<DtoConverterDef> converterDefs) {
        this.converterDefs.addAll(converterDefs);
    }

    public void addEntityDef(EntityDef entityDef) {
        entityDefs.add(entityDef);
    }

    public void putDtoModel(String dtoKey, DtoModel resultModel) {
        models.put(dtoKey,resultModel);
    }

    public void putEnumModel(String origClassName, EnumModel result) {
        enums.put(origClassName,result);
    }

    public boolean containsEnum(String fullName) {
        return enums.containsKey(fullName);
    }

    public EnumModel getEnum(String fullName) {
        return enums.get(fullName);
    }

    public Set<EntityDef> getEntities() {
        return Collections.unmodifiableSet(entityDefs);
    }

    public DtoModel getDtoModelByKey(String key) {
        return models.get(key);
    }

    public boolean containsDtoModel(String key) {
        return models.containsKey(key);
    }

    public Map<String,DtoModel> getDtoModels() {
        return models;
    }

    public Map<String, EnumModel> getEnums() {
        return enums;
    }
}

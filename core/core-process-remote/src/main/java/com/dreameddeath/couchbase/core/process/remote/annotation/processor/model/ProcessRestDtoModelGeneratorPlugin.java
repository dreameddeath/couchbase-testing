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

package com.dreameddeath.couchbase.core.process.remote.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator.UnwrappingStackElement;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.Key;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.AbstractStandardGeneratorPlugin;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.IDtoModelGeneratorPlugin;
import com.dreameddeath.couchbase.core.process.remote.annotation.*;
import com.google.common.collect.Maps;
import com.squareup.javapoet.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by christophe jeunesse on 12/06/2017.
 */

public class ProcessRestDtoModelGeneratorPlugin extends AbstractStandardGeneratorPlugin<RestExpose,Request,Result> implements IDtoModelGeneratorPlugin {
    private static final Map<DtoInOutMode,String> modesSuffix = Maps.newHashMap();
    static{
        modesSuffix.put(DtoInOutMode.IN,"Request");
        modesSuffix.put(DtoInOutMode.OUT,"Response");
    }

    @Override
    protected String getDtoModelType() {
        return RestExpose.REST_EXPOSE_DTO_MODEL_TYPE;
    }

    @Override
    protected Class<RestExpose> getRootAnnot() {
        return RestExpose.class;
    }


    @Override
    protected boolean forceGenerateAbstractClass(ClassInfo entityClassInfo, RestExpose annot) {
        return annot!=null && annot.forceGenerateMode();
    }

    @Override
    protected Class<Request> getFieldInputAnnot() {
        return Request.class;
    }

    @Override
    protected Class<Result> getFieldOutputAnnot() {
        return Result.class;
    }

    @Override
    protected Map<DtoInOutMode, String> classSuffixPerMode() {
        return modesSuffix;
    }

    @Override
    protected SuperClassGenMode getDefaultSuperClassMode() {
        return RestExpose.DEFAULT_SUPERCLASS_GEN_MODE;
    }

    @Override
    protected FieldGenMode getDefaultInFieldGenMode() {
        return RestExpose.DEFAULT_INPUT_GEN_MODE;
    }

    @Override
    protected FieldGenMode getDefaultOutFieldGenMode() {
        return RestExpose.DEFAULT_OUTPUT_GEN_MODE;
    }

    @Override
    protected String getRootAnnotVersion(RestExpose rootAnnot) {
        return rootAnnot.version();
    }

    @Override
    protected SuperClassGenMode getRootAnnotSuperClassGenMode(RestExpose rootAnnot) {
        return rootAnnot.superClassGenMode();
    }

    @Override
    protected String getRootAnnotJsonTypeId(RestExpose rootAnnot) {
        return rootAnnot.jsonTypeId();
    }

    @Override
    protected boolean getRootAnnotIsClassHierarchyRoot(RestExpose rootAnnot) {
        return rootAnnot.isClassHierarchyRoot();
    }

    @Override
    protected FieldGenMode getRootAnnotDefaultInputFieldMode(RestExpose rootAnnot) {
        return rootAnnot.defaultInputFieldMode();
    }

    @Override
    protected FieldGenMode getRootAnnotDefaultOutputFieldMode(RestExpose rootAnnot) {
        return rootAnnot.defaultOutputFieldMode();
    }


    @Override
    protected String getInFieldAnnotVersion(Request fieldAnnot) {
        return fieldAnnot.version();
    }

    @Override
    protected FieldGenMode getInFieldAnnotMode(Request fieldAnnot) {
        return fieldAnnot.mode();
    }

    @Override
    protected FieldGenMode getInFieldAnnotUnwrappedDefaultMode(Request fieldAnnot) {
        return fieldAnnot.unwrappedDefaultMode();
    }

    @Override
    protected String getInFieldAnnotFieldName(Request fieldAnnot) {
        return fieldAnnot.value();
    }

    @Override
    protected String getOutFieldAnnotVersion(Result fieldAnnot) {
        return fieldAnnot.version();
    }

    @Override
    protected FieldGenMode getOutFieldAnnotMode(Result fieldAnnot) {
        return fieldAnnot.mode();
    }

    @Override
    protected FieldGenMode getOutFieldAnnotUnwrappedDefaultMode(Result fieldAnnot) {
        return fieldAnnot.unwrappedDefaultMode();
    }

    @Override
    protected String getOutFieldAnnotFieldName(Result fieldAnnot) {
        return fieldAnnot.value();
    }

    @Override
    protected void generateForAnnot(AbstractDtoModelGenerator abstractDtoModelGenerator, ClassInfo entityClassInfo, RestExpose annot) {
        if(annot.pureSubClassMode()==DtoInOutMode.NONE || annot.pureSubClassMode()==DtoInOutMode.IN||annot.pureSubClassMode()==DtoInOutMode.BOTH) {
            abstractDtoModelGenerator.generate(entityClassInfo, DtoInOutMode.IN, RestExpose.REST_EXPOSE_DTO_MODEL_TYPE, annot.version());
        }
        if(annot.pureSubClassMode()==DtoInOutMode.NONE || annot.pureSubClassMode()==DtoInOutMode.OUT||annot.pureSubClassMode()==DtoInOutMode.BOTH) {
            abstractDtoModelGenerator.generate(entityClassInfo, DtoInOutMode.OUT, RestExpose.REST_EXPOSE_DTO_MODEL_TYPE, annot.version());
        }
    }



    @Override
    public void addTypeInfo(TypeSpec.Builder dtoModelBuilder, ClassInfo clazz, Key key,ClassName dtoSuperClassName) {
        RestExpose annot = getRootAnnotForKey(clazz, key, Collections.emptyList(),false);
        String name;
        if(annot!=null && StringUtils.isNotEmpty(annot.name())){
            name = annot.name();
        }
        else{
            name=clazz.getSimpleName();
        }

        if (annot!=null && annot.pureSubClassMode()==DtoInOutMode.NONE) {
            dtoModelBuilder.addAnnotation(
                    AnnotationSpec.builder(RemoteServiceInfo.class)
                            .addMember("domain", "$S", annot.domain())
                            .addMember("name", "$S", name)
                            .addMember("version", "$S", annot.version())
                            .build()
            );

            dtoModelBuilder.addAnnotation(
                    AnnotationSpec.builder(DtoModelRestApi.class)
                            .addMember("baseClass", "$S", clazz.getFullName())
                            .addMember("rootPath", "$S", annot.rootPath())
                            .addMember("mode", "$T.$L", DtoInOutMode.class, key.getInOutMode().name())
                            .addMember("status", "$T.$L", com.dreameddeath.core.service.annotation.VersionStatus.class, annot.status().name())
                            .addMember("version", "$S", annot.version())
                            .build()
            );
        }
    }

    @Override
    public void addFieldInfo(AbstractDtoModelGenerator.SourceInfoForField fieldInfo, FieldSpec.Builder fieldBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {

    }

    @Override
    public void addSetterInfo(AbstractDtoModelGenerator.SourceInfoForField fieldInfo, MethodSpec.Builder setterBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {

    }

    @Override
    public void addGetterInfo(AbstractDtoModelGenerator.SourceInfoForField fieldInfo, MethodSpec.Builder setterBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {

    }

}

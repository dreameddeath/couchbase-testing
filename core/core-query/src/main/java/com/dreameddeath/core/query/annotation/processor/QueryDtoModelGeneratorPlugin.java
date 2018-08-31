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

package com.dreameddeath.core.query.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator.UnwrappingStackElement;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.Key;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.AbstractStandardPureOutputGeneratorPluginImpl;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.IDtoModelGeneratorPlugin;
import com.dreameddeath.core.query.annotation.DtoModelQueryRestApi;
import com.dreameddeath.core.query.annotation.QueryExpose;
import com.dreameddeath.core.query.annotation.QueryFieldInfo;
import com.dreameddeath.core.query.annotation.RemoteQueryInfo;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.google.common.collect.Maps;
import com.squareup.javapoet.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by christophe jeunesse on 12/06/2017.
 */

public class QueryDtoModelGeneratorPlugin extends AbstractStandardPureOutputGeneratorPluginImpl<QueryExpose,QueryFieldInfo> implements IDtoModelGeneratorPlugin{
    private static final Map<DtoInOutMode,String> modesSuffix = Maps.newHashMap();
    static{
        modesSuffix.put(DtoInOutMode.OUT,"Response");
    }

    @Override
    protected String getDtoModelType() {
        return QueryExpose.REST_EXPOSE_DTO_MODEL_TYPE;
    }

    @Override
    protected Class<QueryExpose> getRootAnnot() {
        return QueryExpose.class;
    }

    @Override
    protected Class<QueryFieldInfo> getFieldOutputAnnot() {
        return QueryFieldInfo.class;
    }

    @Override
    protected Map<DtoInOutMode, String> classSuffixPerMode() {
        return modesSuffix;
    }

    @Override
    protected SuperClassGenMode getDefaultSuperClassMode() {
        return QueryExpose.DEFAULT_SUPERCLASS_GEN_MODE;
    }

    @Override
    protected FieldGenMode getDefaultOutFieldGenMode() {
        return QueryExpose.DEFAULT_OUTPUT_GEN_MODE;
    }

    @Override
    protected String getRootAnnotVersion(QueryExpose rootAnnot) {
        return rootAnnot.version();
    }

    @Override
    protected SuperClassGenMode getRootAnnotSuperClassGenMode(QueryExpose rootAnnot) {
        return rootAnnot.superClassGenMode();
    }

    @Override
    protected String getRootAnnotJsonTypeId(QueryExpose rootAnnot) {
        return rootAnnot.jsonTypeId();
    }

    @Override
    protected boolean getRootAnnotIsClassHierarchyRoot(QueryExpose rootAnnot) {
        return rootAnnot.isClassRootHierarchy();
    }

    @Override
    protected FieldGenMode getRootAnnotDefaultOutputFieldMode(QueryExpose rootAnnot) {
        return rootAnnot.defaultOutputFieldMode();
    }

    @Override
    protected String getOutFieldAnnotVersion(QueryFieldInfo fieldAnnot) {
        return fieldAnnot.version();
    }

    @Override
    protected FieldGenMode getOutFieldAnnotMode(QueryFieldInfo fieldAnnot) {
        return fieldAnnot.mode();
    }

    @Override
    protected FieldGenMode getOutFieldAnnotUnwrappedDefaultMode(QueryFieldInfo fieldAnnot) {
        return fieldAnnot.unwrappedDefaultMode();
    }

    @Override
    protected String getOutFieldAnnotFieldName(QueryFieldInfo fieldAnnot) {
        return fieldAnnot.value();
    }

    @Override
    protected void generateForAnnot(AbstractDtoModelGenerator abstractDtoModelGenerator, ClassInfo entityClassInfo, QueryExpose annot) {
        abstractDtoModelGenerator.generate(entityClassInfo, DtoInOutMode.OUT, QueryExpose.REST_EXPOSE_DTO_MODEL_TYPE, annot.version());
    }

    @Override
    protected boolean forceGenerateAbstractClass(ClassInfo entityClassInfo, QueryExpose annot) {
        return annot!=null && annot.forceGenerateMode();
    }

    @Override
    public void addTypeInfo(TypeSpec.Builder dtoModelBuilder, ClassInfo clazz, Key key,ClassName dtoSuperClassName) {
        QueryExpose annot = getRootAnnotForKey(clazz, key, Collections.emptyList(),false);
        String name;
        if(annot!=null && StringUtils.isNotEmpty(annot.name())){
            name = annot.name();
        }
        else{
            name=clazz.getSimpleName();
        }

        if (annot!=null && !annot.notDirecltyExposed()) {
            dtoModelBuilder.addAnnotation(
                    AnnotationSpec.builder(RemoteQueryInfo.class)
                            .addMember("domain", "$S", annot.domain())
                            .addMember("name", "$S", name)
                            .addMember("version", "$S", annot.version())
                            .build()
            );

            dtoModelBuilder.addAnnotation(
                    AnnotationSpec.builder(DtoModelQueryRestApi.class)
                            .addMember("baseClass", "$S", clazz.getFullName())
                            .addMember("rootPath", "$S", annot.rootPath())
                            .addMember("domain", "$S", annot.domain())
                            .addMember("status", "$T.$L", VersionStatus.class, annot.status().name())
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

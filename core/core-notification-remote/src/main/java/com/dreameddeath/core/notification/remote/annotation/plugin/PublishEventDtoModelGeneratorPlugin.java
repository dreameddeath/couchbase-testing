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

package com.dreameddeath.core.notification.remote.annotation.plugin;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.Key;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.AbstractStandardPureOutputGeneratorPluginImpl;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.IDtoModelGeneratorPlugin;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.notification.annotation.EventOrigModelID;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.remote.annotation.PublishEvent;
import com.dreameddeath.core.notification.remote.annotation.PublishEventField;
import com.google.common.collect.Maps;
import com.squareup.javapoet.*;

import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 26/06/2017.
 */
public class PublishEventDtoModelGeneratorPlugin extends AbstractStandardPureOutputGeneratorPluginImpl<PublishEvent,PublishEventField> implements IDtoModelGeneratorPlugin {
    private static final Map<DtoInOutMode,String> modesSuffix =Maps.newHashMap();
    static{
        modesSuffix.put(DtoInOutMode.OUT,"");
    }
    @Override
    protected String getDtoModelType() {
        return PublishEvent.DTO_MODEL_TYPE;
    }

    @Override
    protected Class<PublishEvent> getRootAnnot() {
        return PublishEvent.class;
    }

    @Override
    protected Class<PublishEventField> getFieldOutputAnnot() {
        return PublishEventField.class;
    }

    @Override
    protected Map<DtoInOutMode, String> classSuffixPerMode() {
        return modesSuffix;
    }

    @Override
    protected SuperClassGenMode getDefaultSuperClassMode() {
        return PublishEvent.DEFAULT_SUPERCLASS_GEN_MODE;
    }

    @Override
    protected FieldGenMode getDefaultOutFieldGenMode() {
        return PublishEvent.DEFAULT_OUTPUT_GEN_MODE;
    }

    @Override
    protected String getRootAnnotVersion(PublishEvent annot) {
        return annot.version();
    }

    @Override
    protected SuperClassGenMode getRootAnnotSuperClassGenMode(PublishEvent annot) {
        return annot.superClassGenMode();
    }

    @Override
    protected String getRootAnnotJsonTypeId(PublishEvent annot) {
        return annot.jsonTypeId();
    }

    @Override
    protected FieldGenMode getRootAnnotDefaultOutputFieldMode(PublishEvent rootAnnotForKey) {
        return rootAnnotForKey.defaultOutputFieldMode();
    }

    @Override
    protected String getOutFieldAnnotVersion(PublishEventField annot) {
        return annot.version();
    }

    @Override
    protected FieldGenMode getOutFieldAnnotMode(PublishEventField annot) {
        return annot.mode();
    }

    @Override
    protected FieldGenMode getOutFieldAnnotUnwrappedDefaultMode(PublishEventField annot) {
        return annot.unwrappedDefaultMode();
    }

    @Override
    protected String getOutFieldAnnotFieldName(PublishEventField annot) {
        return annot.value();
    }

    @Override
    public SuperClassGenMode getSuperClassGeneratorMode(ClassInfo childClass, ClassInfo parentClazz, Key dtoModelKey, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements){
        if(parentClazz.isInstanceOf(Event.class)){
            return SuperClassGenMode.UNWRAP;
        }
        else{
            return super.getSuperClassGeneratorMode(childClass, parentClazz, dtoModelKey, unwrappingStackElements);
        }
    }


    @Override
    protected void generateForAnnot(AbstractDtoModelGenerator abstractDtoModelGenerator, ClassInfo entityClassInfo, PublishEvent annot) {
        abstractDtoModelGenerator.generate(entityClassInfo, DtoInOutMode.OUT, PublishEvent.DTO_MODEL_TYPE, annot.version());
    }

    @Override
    public void addTypeInfo(TypeSpec.Builder dtoModelBuilder, ClassInfo origClass, Key key,ClassName dtoSuperClassName) {
        CouchbaseDocumentStructureReflection reflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(origClass);
        EntityModelId modelId = reflection.getEntityModelId();

        //Add the IEvent interface to the class if parent is a root class of an Event
        if(origClass.isInstanceOf(Event.class) && dtoSuperClassName==null){
            dtoModelBuilder.addSuperinterface(IEvent.class);
        }

        dtoModelBuilder.addAnnotation(
                AnnotationSpec.builder(EventOrigModelID.class)
                .addMember("value","$S",modelId.toString())
                .build()
        );
    }

    @Override
    public void addFieldInfo(AbstractDtoModelGenerator.SourceInfoForField fieldInfo, FieldSpec.Builder fieldBuilder, Key key, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements) {

    }

    @Override
    public void addSetterInfo(AbstractDtoModelGenerator.SourceInfoForField fieldInfo, MethodSpec.Builder setterBuilder, Key key, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements) {

    }

    @Override
    public void addGetterInfo(AbstractDtoModelGenerator.SourceInfoForField fieldInfo, MethodSpec.Builder setterBuilder, Key key, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements) {

    }

}

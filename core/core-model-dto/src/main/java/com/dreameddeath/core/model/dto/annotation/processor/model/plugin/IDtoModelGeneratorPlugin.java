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

package com.dreameddeath.core.model.dto.annotation.processor.model.plugin;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ParameterizedTypeInfo;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator.UnwrappingStackElement;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.Key;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Collection;
import java.util.List;

/**
 * Created by christophe jeunesse on 12/06/2017.
 */
public interface IDtoModelGeneratorPlugin {
    Collection<String> getSupportedAnnotations();

    Key getKey(ClassInfo clazz, DtoInOutMode mode, String type, String version);

    boolean isApplicableToKey(Key dtoModelKey);

    void generateIfNeeded(AbstractDtoModelGenerator abstractDtoModelGenerator, ClassInfo entityClassInfo);

    void addTypeInfo(TypeSpec.Builder dtoModelBuilder, ClassInfo clazz, Key key,ClassName dtoSuperClassName);

    void addFieldInfo(String name, FieldSpec.Builder fieldBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements);

    void addSetterInfo(String name, MethodSpec.Builder setterBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements);

    void addGetterInfo(String name, MethodSpec.Builder setterBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements);

    SuperClassGenMode getSuperClassGeneratorMode(ClassInfo childClass, ClassInfo parentClazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    FieldGenMode getFieldGeneratorMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    FieldGenMode getUnwrappedFieldGenMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    String getFieldEffectiveName(FieldInfo field, Key key, List<UnwrappingStackElement> unwrappingStackElements);

    void addHierarchyBasedTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key, ClassName superClassDtoName);

    FieldGenMode getFieldGeneratorModeDefaultFromClass(ClassInfo rootClassInfo, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);
}

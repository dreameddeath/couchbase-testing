/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.core.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.util.CouchbaseDocumentStructureReflection;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

public class CouchbaseDocumentIntrospector extends JacksonAnnotationIntrospector implements
        Versioned {

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }


    @Override
    /**
     * Helper method for constructing standard {@link com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder}
     * implementation.
     */
    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
        return new DocumentTypeResolverBuilder();
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a)
    {
        PropertyName name = super.findNameForSerialization(a);
        if(name==null) {
            if (a instanceof AnnotatedMethod){
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(CouchbaseDocumentStructureReflection.isReflexible(am.getDeclaringClass())) {
                    if (am.getName().startsWith("get") && (am.getName().length() > 3)) {
                        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo) AbstractClassInfo.getClassInfo(am.getDeclaringClass()));
                        CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getDeclaredFieldByGetterName(am.getName());
                        if ((fieldReflection != null) &&(fieldReflection.getGetter() instanceof MethodInfo)) {
                            name = new PropertyName(fieldReflection.getName());
                        }
                    }
                }
            }
            else if(a instanceof AnnotatedField){
                AnnotatedField af = (AnnotatedField) a;
                if(CouchbaseDocumentStructureReflection.isReflexible(af.getDeclaringClass())) {
                    CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo) AbstractClassInfo.getClassInfo(af.getDeclaringClass()));
                    CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getDeclaredFieldByName(af.getName());
                    if (fieldReflection != null && (fieldReflection.getGetter() instanceof FieldInfo)) {
                        name = new PropertyName(fieldReflection.getName());
                    }
                }
            }
        }

        return name;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a)
    {
        PropertyName name = super.findNameForDeserialization(a);
        if(name==null) {
            if (a instanceof AnnotatedMethod){
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(CouchbaseDocumentStructureReflection.isReflexible(am.getDeclaringClass())) {
                    if (am.getName().startsWith("set")) {
                        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)AbstractClassInfo.getClassInfo(am.getDeclaringClass()));
                        CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getDeclaredFieldBySetterName(am.getName());
                        if((fieldReflection != null) && (fieldReflection.getSetter() instanceof MethodInfo)) {
                            name = new PropertyName(fieldReflection.getName());
                        }
                    }
                }
            }
            else if(a instanceof AnnotatedField){
                AnnotatedField af = (AnnotatedField) a;
                if(CouchbaseDocumentStructureReflection.isReflexible(af.getDeclaringClass())) {
                    CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo) AbstractClassInfo.getClassInfo(af.getDeclaringClass()));
                    CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getDeclaredFieldByName(af.getName());
                    if (fieldReflection != null && (fieldReflection.getSetter() instanceof FieldInfo)) {
                        name = new PropertyName(fieldReflection.getName());
                    }
                }
            }
        }

        return name;
    }
}
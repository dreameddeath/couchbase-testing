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

package com.dreameddeath.core.couchbase;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CouchbaseDocumentIntrospector extends JacksonAnnotationIntrospector implements
        Versioned {

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }


    @Override
    /**
     * Helper method for constructing standard {@link TypeResolverBuilder}
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
                if(am.getName().startsWith("get") && (am.getName().length()>3)){
                    String fieldName = am.getName().substring(3,4).toLowerCase()+am.getName().substring(4);

                    for(Field field: am.getDeclaringClass().getDeclaredFields()) {
                        DocumentProperty fieldProp = field.getAnnotation(DocumentProperty.class);
                        if(fieldProp==null) continue;
                        if(am.getName().equals(fieldProp.getter())){
                            name = new PropertyName(fieldProp.value());
                            break;
                        }
                        else if(fieldName.equals(fieldProp.value())){
                            name=new PropertyName(fieldProp.value());
                        }
                    }
                }
            }
            else if(a instanceof AnnotatedField){
                AnnotatedField af = (AnnotatedField) a;
                DocumentProperty fieldProp = af.getAnnotation(DocumentProperty.class);
                if(((af.getModifiers() & Modifier.PUBLIC)!=0)&&(fieldProp!=null)){
                    name=new PropertyName(fieldProp.value());
                }
            }
        }

        return name;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a)
    {
        PropertyName name = super.findNameForSerialization(a);
        if(name==null) {
            if (a instanceof AnnotatedMethod){
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(am.getName().startsWith("set")) {
                    for(Field field: am.getDeclaringClass().getDeclaredFields()) {
                        DocumentProperty fieldProp = field.getAnnotation(DocumentProperty.class);
                        if((fieldProp!=null) && (am.getName().equals(fieldProp.setter()))){
                            name = new PropertyName(fieldProp.value());
                        }
                    }
                }
            }
            else if(a instanceof AnnotatedField){
                AnnotatedField af = (AnnotatedField) a;
                DocumentProperty fieldProp = af.getAnnotation(DocumentProperty.class);
                if(((af.getModifiers() & Modifier.PUBLIC)!=0)&&(fieldProp!=null)){
                    name=new PropertyName(fieldProp.value());
                }
            }
        }

        return name;
    }
}
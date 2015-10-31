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

package com.dreameddeath.core.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.reflect.Type;

/**
 * Created by Christophe Jeunesse on 30/10/2015.
 */
public class DefaultGetterSetterIntrospector extends JacksonAnnotationIntrospector {
    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }

    private PropertyName findMathingField(AnnotatedMethod am, String propertyName, Type type){
        for(AnnotatedField field:am.getContextClass().fields()){
            if(!type.equals(field.getGenericType())){
                continue;
            }

            JsonProperty prop = field.getAnnotation(JsonProperty.class);
            if(prop!=null) {
                if (propertyName.equalsIgnoreCase(prop.value()) ||propertyName.equalsIgnoreCase(field.getName())){
                    return new PropertyName(prop.value());
                }
            }
        }
        return null;
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        PropertyName name = super.findNameForSerialization(a);
        if(name==null){
            if (a instanceof AnnotatedMethod) {
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(am.getAnnotated().getName().startsWith("get") && am.getAnnotated().getParameterCount()==0  && am.hasReturnType()){

                    name = findMathingField(am, am.getAnnotated().getName().substring("get".length()), am.getGenericReturnType());
                }
            }
        }
        return name;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        PropertyName name = super.findNameForDeserialization(a);
        if(name==null){
            if (a instanceof AnnotatedMethod) {
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(am.getAnnotated().getName().startsWith("set") && am.getParameterCount()==1 && !am.hasReturnType()){
                    name = findMathingField(am, am.getAnnotated().getName().substring("set".length()), am.getGenericParameterType(0));
                }
            }
        }
        return name;
    }
}

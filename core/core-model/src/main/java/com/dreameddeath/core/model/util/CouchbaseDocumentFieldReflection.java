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

package com.dreameddeath.core.model.util;

import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MemberInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ParameterizedTypeInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.annotation.DocumentProperty;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 04/01/2015.
 */
public class CouchbaseDocumentFieldReflection {
    private static final String[] GETTER_PREFIXES={"get","is"};
    private static final String[] SETTER_PREFIXES={"set","is"};
    private String name;
    private FieldInfo field;
    private ParameterizedTypeInfo effectiveType;

    private MemberInfo getter;
    private MemberInfo setter;


    protected String nameBuilder(String name, String prefix){
        if(name.startsWith("_")){
            name = name.substring(1);
        }
        return prefix+name.substring(0,1).toUpperCase()+name.substring(1);
    }


    public MethodInfo fieldGetterFinder(){
        MethodInfo result = null;
        if(field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = field.getAnnotation(DocumentProperty.class);
            String getter = prop.getter();
            if(StringUtils.isNotEmpty(getter)){
                result=field.getDeclaringClassInfo().getDeclaredMethod(getter);
            }
            else {
                String name = nameBuilder(this.name,"get");
                result= field.getDeclaringClassInfo().getDeclaredMethod(name);
                if(result==null){
                    name = nameBuilder(field.getName(),"is");
                    result = field.getDeclaringClassInfo().getDeclaredMethod(name);
                }
            }
        }

        if(result==null){
            String name = nameBuilder(field.getName(),"get");
            result = field.getDeclaringClassInfo().getDeclaredMethod(name);
            if(result==null){
                name = nameBuilder(field.getName(),"is");
                result = field.getDeclaringClassInfo().getDeclaredMethod(name);
            }
        }

        return result;
    }

    public MethodInfo fieldSetterFinder(){
        MethodInfo result = null;
        if(field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = field.getAnnotation(DocumentProperty.class);
            String setter = prop.setter();
            if(StringUtils.isNotEmpty(setter)){
                result = field.getDeclaringClassInfo().getDeclaredMethod(setter, effectiveType);
            }
            else {
                String name = nameBuilder(this.name, "set");
                result= field.getDeclaringClassInfo().getDeclaredMethod(name,effectiveType);
                if(result==null){
                    name = nameBuilder(field.getName(),"is");
                    result = field.getDeclaringClassInfo().getDeclaredMethod(name,effectiveType);
                }
            }
        }

        if(result==null) {
            String name = nameBuilder(field.getName(), "set");
            result = field.getDeclaringClassInfo().getDeclaredMethod(name,effectiveType);
            if(result==null){
                name = nameBuilder(field.getName(),"is");
                result = field.getDeclaringClassInfo().getDeclaredMethod(name,effectiveType);
            }
        }
        return result;
    }

    public CouchbaseDocumentFieldReflection(FieldInfo fieldInfo) {
        name = fieldInfo.getAnnotation(DocumentProperty.class).value();
        if(StringUtils.isEmpty(name)){
            name = fieldInfo.getName();
        }
        field = fieldInfo;
        getter = fieldGetterFinder();
        if(getter==null){
            if(fieldInfo.isPublic()){
                getter = fieldInfo;
                effectiveType = fieldInfo.getType();
            }
            else{
                throw new RuntimeException("Cannot find getter of field "+name+ " for entity "+fieldInfo.getDeclaringClassInfo().getFullName());
            }
        }
        else{
            effectiveType = ((MethodInfo)getter).getReturnType();
        }

        setter= fieldSetterFinder();
        if(setter==null) {
            if (fieldInfo.isPublic()) {
                setter = fieldInfo;
            }
            else {
                throw new RuntimeException("Cannot find setter of field " + name + " for entity" + fieldInfo.getDeclaringClassInfo().getFullName());
            }
        }
    }

    public String getName() {
        return name;
    }

    public FieldInfo getField() {
        return field;
    }

    public Class<?> getEffectiveTypeClass() {
        return getEffectiveTypeInfo().getMainType().getCurrentClass();
    }

    public ParameterizedTypeInfo getEffectiveTypeInfo() {
        return effectiveType;
    }

    public MemberInfo getGetter() {
        return getter;
    }

    public String buildGetterCode(){
        if(getter instanceof FieldInfo){
            return getGetterName();
        }
        else{
            return getGetterName()+"()";
        }
    }

    public String getGetterName(){
        return getter.getName();
    }

    public MemberInfo getSetter() {
        return setter;
    }

    public String getSetterName(){
        return setter.getName();
    }

    public boolean isCollection() {
        return field.getType().isAssignableTo(Collection.class);
    }

    public Class<?> getCollectionElementClass() {
        return getCollectionElementTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getCollectionElementTypeInfo() {
        return field.getType().getMainTypeGeneric(0);
    }

    public boolean isMap() {
        return field.getType().isAssignableTo(Map.class);
    }

    public Class<?> getMapKeyClass() {
        return getMapKeyTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getMapKeyTypeInfo() {
        return field.getType().getMainTypeGeneric(0);
    }

    public Class<?> getMapValueClass() {
        return getMapValueTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getMapValueTypeInfo() {
        return field.getType().getMainTypeGeneric(1);
    }
}

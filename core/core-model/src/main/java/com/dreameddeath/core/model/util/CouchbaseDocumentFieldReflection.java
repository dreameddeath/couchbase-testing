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

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.MemberInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.ParameterizedTypeInfo;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 04/01/2015.
 */
public class CouchbaseDocumentFieldReflection {
    private String _name;
    private FieldInfo _field;
    private ParameterizedTypeInfo _effectiveType;

    private MemberInfo _getter;
    private MemberInfo _setter;


    protected String nameBuilder(String name, String prefix){
        if(name.startsWith("_")){
            name = name.substring(1);
        }
        return prefix+name.substring(0,1).toUpperCase()+name.substring(1);
    }


    public MethodInfo fieldGetterFinder(){
        MethodInfo result = null;
        if(_field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = _field.getAnnotation(DocumentProperty.class);
            String getter = prop.getter();
            if((getter!=null)&& !getter.equals("")){
                result=_field.getDeclaringClassInfo().getDeclaredMethod(getter);
            }
            else {
                String name = nameBuilder(prop.value(),"get");
                result= _field.getDeclaringClassInfo().getDeclaredMethod(name);
            }
        }

        if(result==null){
            String name = nameBuilder(_field.getName(),"get");
            result = _field.getDeclaringClassInfo().getDeclaredMethod(name);
        }

        return result;
    }

    public MethodInfo fieldSetterFinder(){
        MethodInfo result = null;
        if(_field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = _field.getAnnotation(DocumentProperty.class);
            String setter = prop.setter();
            if((setter!=null)&& !setter.equals("")){
                result = _field.getDeclaringClassInfo().getDeclaredMethod(setter, _effectiveType);
            }
            else {
                String name = nameBuilder(prop.value(), "set");
                result= _field.getDeclaringClassInfo().getDeclaredMethod(name,_effectiveType);
            }
        }

        if(result==null) {
            String name = nameBuilder(_field.getName(), "set");
            result = _field.getDeclaringClassInfo().getDeclaredMethod(name,_effectiveType);
        }
        return result;
    }

    public CouchbaseDocumentFieldReflection(FieldInfo fieldInfo) {
        _name = fieldInfo.getAnnotation(DocumentProperty.class).value();
        _field = fieldInfo;
        _getter = fieldGetterFinder();
        if(_getter==null){
            if(fieldInfo.isPublic()){
                _getter = fieldInfo;
                _effectiveType = fieldInfo.getType();
            }
            else{
                throw new RuntimeException("Cannot find getter of field "+_name+ " for entity "+fieldInfo.getDeclaringClassInfo().getFullName());
            }
        }
        else{
            _effectiveType = ((MethodInfo)_getter).getReturnType();
        }

        _setter= fieldSetterFinder();
        if(_setter==null) {
            if (fieldInfo.isPublic()) {
                _setter = fieldInfo;
            }
            else {
                throw new RuntimeException("Cannot find setter of field " + _name + " for entity" + fieldInfo.getDeclaringClassInfo().getFullName());
            }
        }
    }

    public String getName() {
        return _name;
    }

    public FieldInfo getField() {
        return _field;
    }

    public Class<?> getEffectiveTypeClass() {
        return getEffectiveTypeInfo().getMainType().getCurrentClass();
    }

    public ParameterizedTypeInfo getEffectiveTypeInfo() {
        return _effectiveType;
    }

    public MemberInfo getGetter() {
        return _getter;
    }

    public String buildGetterCode(){
        if(_getter instanceof FieldInfo){
            return getGetterName();
        }
        else{
            return getGetterName()+"()";
        }
    }

    public String getGetterName(){
        return _getter.getName();
    }

    public MemberInfo getSetter() {
        return _setter;
    }

    public String getSetterName(){
        return _setter.getName();
    }

    public boolean isCollection() {
        return _field.getType().isAssignableTo(Collection.class);
    }

    public Class<?> getCollectionElementClass() {
        return getCollectionElementTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getCollectionElementTypeInfo() {
        return _field.getType().getMainTypeGeneric(0);
    }

    public boolean isMap() {
        return _field.getType().isAssignableTo(Map.class);
    }

    public Class<?> getMapKeyClass() {
        return getMapKeyTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getMapKeyTypeInfo() {
        return _field.getType().getMainTypeGeneric(0);
    }

    public Class<?> getMapValueClass() {
        return getMapValueTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getMapValueTypeInfo() {
        return _field.getType().getMainTypeGeneric(1);
    }
}

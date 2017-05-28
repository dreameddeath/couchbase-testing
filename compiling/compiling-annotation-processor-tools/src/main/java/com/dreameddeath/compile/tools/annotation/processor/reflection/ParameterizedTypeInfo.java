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

package com.dreameddeath.compile.tools.annotation.processor.reflection;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class ParameterizedTypeInfo{
    private static Logger LOG= LoggerFactory.getLogger(ParameterizedTypeInfo.class);

    private static final Map<TypeMirror,ParameterizedTypeInfo> typeMirrorCache=new HashMap<>();

    public static ParameterizedTypeInfo getParameterizedTypeInfo(TypeMirror typeMirror){
        synchronized (typeMirrorCache) {
            if(!typeMirrorCache.containsKey(typeMirror)) {
                return new ParameterizedTypeInfo(typeMirror);
            }
            return typeMirrorCache.get(typeMirror);
        }
    }

    public static ParameterizedTypeInfo getParameterizedTypeInfo(Type genericSuperclass) {
        return new ParameterizedTypeInfo(genericSuperclass);
    }

    private final String methodParamName;
    private final Type type;
    private final String typeParamName;
    private final List<AbstractClassInfo> parameterizedInfosList=new ArrayList<>();
    private final List<ParameterizedTypeInfo> intersectionInfoList=new ArrayList<>();
    private final List<ParameterizedTypeInfo> parametersGenericsInfo = new ArrayList<>();

    private void addType(DeclaredType type){
        if(type instanceof IntersectionType) {
            for(TypeMirror mirror : ((IntersectionType)type).getBounds()){
                intersectionInfoList.add(ParameterizedTypeInfo.getParameterizedTypeInfo(mirror));
            }
        }
        else{
            if(type.asElement().getKind().isInterface()){
                AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement)type.asElement());
                parameterizedInfosList.add(classInfo);
            }
            else if(type.asElement().getKind().isClass()){
                parameterizedInfosList.add(AbstractClassInfo.getClassInfo((TypeElement) type.asElement()));
            }
        }

        for(TypeMirror parameterElement:type.getTypeArguments()){
            parametersGenericsInfo.add(getParameterizedTypeInfo(parameterElement));
        }
    }

    private void addType(TypeMirror type) {
        if(type instanceof javax.lang.model.type.WildcardType){
            TypeMirror upperBound = ((javax.lang.model.type.WildcardType) type).getExtendsBound();
            if((upperBound!=null) && (upperBound instanceof DeclaredType)){
                addType((DeclaredType)upperBound);
            }
        }
        else if(type instanceof DeclaredType){
            addType((DeclaredType) type);
        }
        else if(type instanceof javax.lang.model.type.TypeVariable){
            TypeMirror upperBound = ((javax.lang.model.type.TypeVariable) type).getUpperBound();
            if((upperBound!=null) && (upperBound instanceof DeclaredType)){
                addType((DeclaredType) upperBound);
            }
        }
    }

    public ParameterizedTypeInfo(TypeParameterElement typeParameter) {
        methodParamName = null;
        typeParamName=typeParameter.getSimpleName().toString();
        for(TypeMirror subType:typeParameter.getBounds()){
            addType(subType);
        }
        if(parameterizedInfosList.size()>0) {
            type = getMainType().getCurrentClass();
        }
        else{
            type=null;
        }
    }

    private ParameterizedTypeInfo(TypeMirror typeMirror){
        methodParamName = null;
        typeMirrorCache.put(typeMirror,this);
        if(typeMirror instanceof javax.lang.model.type.TypeVariable) {
            typeParamName = ((javax.lang.model.type.TypeVariable) typeMirror).asElement().getSimpleName().toString();
        }
        else{
            typeParamName=null;
        }
        addType(typeMirror);
        if(parameterizedInfosList.size()>0) {
            type = getMainType().getCurrentClass();
        }
        else{
            type=null;
        }
    }

    private ParameterizedTypeInfo(ParameterizedType parameterizedType){
        this((Type)parameterizedType);
    }


    public ParameterizedTypeInfo(ParameterizedTypeInfo source,String methodParamName){
        this.methodParamName = methodParamName;
        this.typeParamName=null;
        this.type = source.type;
        this.parameterizedInfosList.addAll(source.parameterizedInfosList);
        this.parametersGenericsInfo.addAll(source.parametersGenericsInfo);

    }

    private void addType(Class clazz){
        parameterizedInfosList.add(AbstractClassInfo.getClassInfo(clazz));
        for(TypeVariable parameterElement:clazz.getTypeParameters()){
            parametersGenericsInfo.add(new ParameterizedTypeInfo(parameterElement));
        }
    }

    public ParameterizedTypeInfo(Type paramType){
        methodParamName =null;
        typeParamName=null;
        type = paramType;
        if(paramType instanceof Class){
            addType((Class)paramType);
        }
        else if(paramType instanceof TypeVariable){
            for(Type parentType :((java.lang.reflect.TypeVariable)paramType).getBounds()){
                if(parentType instanceof Class){
                    addType((Class)parentType);
                }
            }
        }
        else if(paramType instanceof java.lang.reflect.WildcardType){
            for(Type parentType:((java.lang.reflect.WildcardType)paramType).getUpperBounds()){
                if(parentType instanceof Class){
                    addType((Class)parentType);
                }
            }
        }
        else if(paramType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)paramType;
            ParameterizedTypeInfo rawTypeInfo = new ParameterizedTypeInfo(parameterizedType.getRawType());
            parameterizedInfosList.addAll(rawTypeInfo.parameterizedInfosList);
            for(Type genericParam:parameterizedType.getActualTypeArguments()){
                parametersGenericsInfo.add(new ParameterizedTypeInfo(genericParam));
            }
        }
    }

    public List<AbstractClassInfo> getParameterizedInfosList() {
        return parameterizedInfosList;
    }

    public AbstractClassInfo getMainType(){
        return parameterizedInfosList.get(0);
    }



    public ParameterizedTypeInfo getMainTypeGeneric(int pos){
        return parametersGenericsInfo.get(pos);
    }


    public boolean isAssignableTo(Class clazz){
        return AbstractClassInfo.getClassInfo(clazz).isAssignableFrom(getMainType());
    }

    public String getMethodParamName() {
        return methodParamName;
    }

    public String getTypeParamName(){
        return typeParamName;
    }

    public Type getType() {
        return type;
    }

}

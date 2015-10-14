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


import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class ParameterizedTypeInfo{
    private String name;
    private Type type;
    List<AbstractClassInfo> parameterizedInfosList=new ArrayList<>();
    List<ParameterizedTypeInfo> parametersGenericsInfo = new ArrayList<>();

    private void addType(DeclaredType type){
        if(type.asElement().getKind().isInterface()){
            parameterizedInfosList.add(AbstractClassInfo.getClassInfo((TypeElement)type.asElement()));
        }
        else if(type.asElement().getKind().isClass()){
            parameterizedInfosList.add(AbstractClassInfo.getClassInfo((TypeElement) type.asElement()));
        }

        for(TypeMirror parameterElement:type.getTypeArguments()){
            parametersGenericsInfo.add(new ParameterizedTypeInfo(parameterElement));
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
    }

    public ParameterizedTypeInfo(TypeParameterElement typeParameter) {
        for(TypeMirror subType:typeParameter.getBounds()){
            addType(subType);
        }
        if(parameterizedInfosList.size()>0) {
            type = getMainType().getCurrentClass();
        }
    }

    public ParameterizedTypeInfo(TypeMirror typeMirror){
        addType(typeMirror);
        if(parameterizedInfosList.size()>0) {
            type = getMainType().getCurrentClass();
        }
    }

    private void addType(Class clazz){
        parameterizedInfosList.add(AbstractClassInfo.getClassInfo(clazz));
        for(TypeVariable parameterElement:clazz.getTypeParameters()){
            parametersGenericsInfo.add(new ParameterizedTypeInfo(parameterElement));
        }
    }

    public ParameterizedTypeInfo(Type paramType){
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public Type getType() {
        return type;
    }
}

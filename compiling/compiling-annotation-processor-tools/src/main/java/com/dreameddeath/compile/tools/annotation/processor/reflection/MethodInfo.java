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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class MethodInfo extends MemberInfo {
    public static MethodInfo getMethodInfo(ExecutableElement elt){
        AbstractClassInfo classInfo = ClassInfo.getClassInfo((TypeElement) elt.getEnclosingElement());
        return new MethodInfo(classInfo,elt);
    }

    private String name;
    private AbstractClassInfo parent;
    private Method method=null;
    private ExecutableElement elementMethod=null;
    private ParameterizedTypeInfo returnType=null;
    private List<ParameterizedTypeInfo> methodParameters=new ArrayList<>();

    private void init(){
        if(elementMethod!=null){
            name = elementMethod.getSimpleName().toString();
            for(VariableElement parameter:elementMethod.getParameters()) {
                ParameterizedTypeInfo paramInfo = ParameterizedTypeInfo.getParameterizedTypeInfo(parameter.asType());

                methodParameters.add(new ParameterizedTypeInfo(paramInfo,parameter.getSimpleName().toString()));

            }
            TypeMirror methodReturnType = elementMethod.getReturnType();
            returnType=ParameterizedTypeInfo.getParameterizedTypeInfo(methodReturnType);
        }
        else{
            name = method.getName();
            for(Parameter parameter:method.getParameters()){//method.getGenericParameterTypes()){
                ParameterizedTypeInfo paramInfo = new ParameterizedTypeInfo(parameter.getParameterizedType());
                methodParameters.add(new ParameterizedTypeInfo(paramInfo,parameter.getName()));
            }
            Type methodGenericReturnType=method.getGenericReturnType();
            returnType = new ParameterizedTypeInfo(methodGenericReturnType);
        }


    }

    public MethodInfo(AbstractClassInfo parent,ExecutableElement element){
        super(parent,element);
        this.parent = parent;
        elementMethod = element;
        init();
    }

    public MethodInfo(AbstractClassInfo parent,Method method){
        super(parent,method);
        this.parent = parent;
        this.method = method;
        init();
    }

    public String getName() {
        return name;
    }

    public String getFullName(){
        return parent.getFullName()+"."+name;
    }

    public ParameterizedTypeInfo getReturnType(){
        return returnType;
    }

    public List<ParameterizedTypeInfo> getMethodParameters(){
        return methodParameters;
    }

    public ParameterizedTypeInfo getMethodParamByName(String name){
        for(ParameterizedTypeInfo paramInfo:getMethodParameters()){
            if(name.equals(paramInfo.getName())){
                return paramInfo;
            }
        }
        return null;
    }
}

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

package com.dreameddeath.core.tools.annotation.processor.reflection;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class MethodInfo extends MemberInfo {
    private String _name;
    private AbstractClassInfo _parent;
    private Method _method=null;
    private ExecutableElement _elementMethod=null;
    private ParameterizedTypeInfo _returnType=null;
    private List<ParameterizedTypeInfo> _methodParameters=new ArrayList<>();

    private void init(){
        if(_elementMethod!=null){
            _name = _elementMethod.getSimpleName().toString();
            for(VariableElement parameter:_elementMethod.getParameters()) {
                ParameterizedTypeInfo paramInfo = new ParameterizedTypeInfo(parameter.asType());
                paramInfo.setName(parameter.getSimpleName().toString());
                _methodParameters.add(paramInfo);

            }
            TypeMirror returnType = _elementMethod.getReturnType();
            _returnType=new ParameterizedTypeInfo(returnType);
            returnType.getKind();
        }
        else{
            _name = _method.getName();
            for(Type parameter:_method.getGenericParameterTypes()){
                ParameterizedTypeInfo paramInfo = new ParameterizedTypeInfo(parameter);
                paramInfo.setName(parameter.getTypeName());
                _methodParameters.add(paramInfo);
            }
            Type returnType=_method.getGenericReturnType();
            _returnType = new ParameterizedTypeInfo(returnType);
            returnType.getTypeName();
        }


    }

    public MethodInfo(AbstractClassInfo parent,ExecutableElement element){
        super(parent,element);
        _parent = parent;
        _elementMethod = element;
        init();
    }

    public MethodInfo(AbstractClassInfo parent,Method method){
        super(parent,method);
        _parent = parent;
        _method = method;
        init();
    }

    public String getName() {
        return _name;
    }

    public String getFullName(){
        return _parent.getFullName()+"."+_name;
    }

    public ParameterizedTypeInfo getReturnType(){
        return _returnType;
    }

    public List<ParameterizedTypeInfo> getMethodParameters(){
        return _methodParameters;
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

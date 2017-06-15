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

package com.dreameddeath.compile.tools.annotation.processor.reflection;

import javax.lang.model.element.VariableElement;
import java.lang.reflect.Field;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class FieldInfo extends MemberInfo {
    private final ClassInfo parent;
    private String name;
    private VariableElement variableElement=null;
    private Field field=null;
    private ParameterizedTypeInfo typeInfo=null;

    protected void init(){
        if(field!=null){
            name = field.getName();
            typeInfo = new ParameterizedTypeInfo(field.getGenericType());
        }

        if(variableElement!=null){
            name = variableElement.getSimpleName().toString();
            typeInfo =  ParameterizedTypeInfo.getParameterizedTypeInfo(variableElement.asType());
        }
    }

    public FieldInfo(ClassInfo parent,VariableElement element){
        super(parent,element);
        this.parent = parent;
        variableElement=element;
        init();
    }

    public FieldInfo(ClassInfo parent,Field field){
        super(parent,field);
        this.parent = parent;
        this.field = field;
        init();
    }

    public String getName(){
        return name;
    }

    public String getFullName(){
        return parent.getFullName()+"."+getName();
    }

    public ParameterizedTypeInfo getType() {
        return typeInfo;
    }
    @Override
    public ClassInfo getDeclaringClassInfo(){
        return parent;
    }

}

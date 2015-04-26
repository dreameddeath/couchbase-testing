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

package com.dreameddeath.core.tools.annotation.processor.reflection;

import javax.lang.model.element.VariableElement;
import java.lang.reflect.Field;

/**
 * Created by ceaj8230 on 07/03/2015.
 */
public class FieldInfo extends MemberInfo {
    private final AbstractClassInfo _parent;
    private String _name;
    private VariableElement _variableElement=null;
    private Field _field=null;
    private ParameterizedTypeInfo _typeInfo=null;

    protected void init(){
        if(_field!=null){
            _name = _field.getName();
            _typeInfo = new ParameterizedTypeInfo(_field.getGenericType());
        }

        if(_variableElement!=null){
            _name = _variableElement.getSimpleName().toString();
            _typeInfo = new ParameterizedTypeInfo(_variableElement.asType());
        }
    }

    private FieldInfo(AbstractClassInfo parent,VariableElement element){
        super(parent,element);
        _parent = parent;
        _variableElement=element;
        init();
    }


    public FieldInfo(ClassInfo parent,VariableElement element){
        this((AbstractClassInfo) parent, element);
    }


    private FieldInfo(AbstractClassInfo parent,Field field){
        super(parent,field);
        _parent = parent;
        _field = field;
        init();
    }


    public FieldInfo(ClassInfo parent,Field field){
        this((AbstractClassInfo)parent,field);
    }

    public String getName(){
        return _name;
    }

    public String getFullName(){
        return _parent.getFullName()+"."+getName();
    }

    public ParameterizedTypeInfo getType() {
        return _typeInfo;
    }

}

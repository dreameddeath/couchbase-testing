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

import com.dreameddeath.core.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.core.tools.annotation.processor.AnnotationElementType;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class ClassInfo extends AbstractClassInfo {
    private ClassInfo _superClass=null;
    private List<FieldInfo> _declaredFields = null;

    @Override
    public boolean isInterface() {
        return false;
    }

    private void init(){
        if(getTypeElement()!=null){
            TypeMirror superClass = getTypeElement().getSuperclass();
            if(superClass.getKind()!= TypeKind.NONE){
                _superClass = (ClassInfo)AbstractClassInfo.getClassInfo((TypeElement) ((DeclaredType)superClass).asElement());
            }
        }
        else if(getCurrentClass().getSuperclass()!=null){
            _superClass = (ClassInfo)AbstractClassInfo.getClassInfo(getCurrentClass().getSuperclass());
        }
    }

    protected ClassInfo(TypeElement element){
        super(element);
        init();
    }

    protected ClassInfo(Class<?> clazz){
        super(clazz);
        init();
    }

    @Override
    public MethodInfo getMethod(String name,ParameterizedTypeInfo ...infos){
        MethodInfo foundMethod = getDeclaredMethod(name,infos);
        if((foundMethod==null)&&(_superClass!=null)){
            foundMethod = _superClass.getMethod(name,infos);
        }
        if(foundMethod==null){
            for(InterfaceInfo parentInterface:getParentInterfaces()){
                foundMethod = parentInterface.getMethod(name,infos);
                if(foundMethod!=null) break;
            }
        }
        return foundMethod;
    }

    public List<FieldInfo> getDeclaredFields(){
        if(_declaredFields ==null){
            _declaredFields =new ArrayList<>();
            if(getTypeElement()!=null){
                for(Element elt:getTypeElement().getEnclosedElements()){
                    try {
                        AnnotationElementType eltType = AnnotationElementType.getTypeOf(elt);
                        if(eltType.equals(AnnotationElementType.FIELD)){
                            _declaredFields.add(new FieldInfo(this,(VariableElement) elt));
                        }
                    }
                    catch(AnnotationProcessorException e){
                        throw new RuntimeException("Unhandled element",e);
                    }
                }
            }
            else{
                for(Field declaredField: getCurrentClass().getDeclaredFields()){
                    _declaredFields.add(new FieldInfo(this,declaredField));
                }
            }
        }
        return Collections.unmodifiableList(_declaredFields);
    }

    public FieldInfo getFieldByName(String name){
        for(FieldInfo field:getDeclaredFields()){
            if(name.equals(field.getName())){
                return field;
            }
        }
        if(_superClass!=null){
            return _superClass.getFieldByName(name);
        }
        return null;
    }

    public ClassInfo getSuperClass() {
        return _superClass;
    }
}

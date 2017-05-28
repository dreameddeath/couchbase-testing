/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.compile.tools.annotation.processor.reflection;

import com.dreameddeath.compile.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.lang.model.element.ElementKind.ENUM;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class ClassInfo extends AbstractClassInfo {
    private static final Logger LOG = LoggerFactory.getLogger(ClassInfo.class);

    private ClassInfo superClass=null;
    private boolean isEnum=false;
    private boolean isAbstract=false;
    private List<FieldInfo> declaredFields = null;
    private ParameterizedTypeInfo parameterizedSuperClass=null;

    @Override
    public boolean isInterface() {
        return false;
    }

    private void init(){
        if(getTypeElement()!=null){
            TypeMirror superClassTypeMirror = getTypeElement().getSuperclass();
            if(superClassTypeMirror.getKind()!= TypeKind.NONE){
                superClass = (ClassInfo) getClassInfo((TypeElement) ((DeclaredType)superClassTypeMirror).asElement());
                try {
                    parameterizedSuperClass = ParameterizedTypeInfo.getParameterizedTypeInfo(superClassTypeMirror);
                }
                catch(Throwable e){
                    LOG.warn("Cannot get parameterized type form super type <"+superClassTypeMirror+"> from <"+getTypeElement()+">");
                }
            }
            isEnum=getTypeElement().getKind()==ENUM;
            isAbstract=getTypeElement().getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT);
        }
        else if(getCurrentClass().getSuperclass()!=null){
            superClass = (ClassInfo) getClassInfo(getCurrentClass().getSuperclass());
            parameterizedSuperClass = ParameterizedTypeInfo.getParameterizedTypeInfo(getCurrentClass().getGenericSuperclass());
            isEnum = this.getClass().isEnum();
            isAbstract = Modifier.isAbstract(this.getClass().getModifiers());
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
        if((foundMethod==null)&&(superClass!=null)){
            foundMethod = superClass.getMethod(name,infos);
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
        if(declaredFields ==null){
            declaredFields =new ArrayList<>();
            if(getTypeElement()!=null){
                for(Element elt:getTypeElement().getEnclosedElements()){
                    try {
                        AnnotationElementType eltType = AnnotationElementType.getTypeOf(elt);
                        if(eltType.equals(AnnotationElementType.FIELD)){
                            declaredFields.add(new FieldInfo(this,(VariableElement) elt));
                        }
                    }
                    catch(AnnotationProcessorException e){
                        throw new RuntimeException("Unhandled element",e);
                    }
                }
            }
            else{
                for(Field declaredField: getCurrentClass().getDeclaredFields()){
                    declaredFields.add(new FieldInfo(this,declaredField));
                }
            }
        }
        return Collections.unmodifiableList(declaredFields);
    }

    public FieldInfo getFieldByName(String name){
        for(FieldInfo field:getDeclaredFields()){
            if(name.equals(field.getName())){
                return field;
            }
        }
        if(superClass!=null){
            return superClass.getFieldByName(name);
        }
        return null;
    }

    public ClassInfo getSuperClass() {
        return superClass;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public boolean isAbstract(){
        return isAbstract;
    }

    public ParameterizedTypeInfo getParameterizedSuperClass() {
        return parameterizedSuperClass;
    }
}

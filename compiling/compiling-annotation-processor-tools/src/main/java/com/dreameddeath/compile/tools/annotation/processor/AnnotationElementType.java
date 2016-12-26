/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.compile.tools.annotation.processor;

import com.dreameddeath.compile.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.compile.tools.annotation.processor.reflection.*;
import com.dreameddeath.core.java.utils.ClassUtils;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;

public enum AnnotationElementType {
    PACKAGE,
    ANNOTATION,
    CLASS,
    INTERFACE,
    METHOD,
    FIELD,
    METHOD_PARAMETER,
    GENERIC_PARAMETER;

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationElementType.class);

    public static ThreadLocal<Elements> CURRENT_ELEMENT_UTILS=new ThreadLocal<>();

    public static AnnotationElementType getTypeOf(Element element) throws AnnotationProcessorException {
        if(element instanceof PackageElement){
            return PACKAGE;
        }
        else if(element instanceof TypeParameterElement){
            return GENERIC_PARAMETER;
        }
        else if(element instanceof TypeElement){
            if(element.getKind().equals(ElementKind.ANNOTATION_TYPE)){
                return ANNOTATION;
            }
            if(element.getKind().isInterface()){
                return INTERFACE;
            }
            else {
                return CLASS;
            }
        }
        else if(element instanceof ExecutableElement){
            return METHOD;
        }
        else if(element instanceof VariableElement){
            Element parentElement = element.getEnclosingElement();
            switch(getTypeOf(parentElement)){
                case METHOD:
                    return METHOD_PARAMETER;
                case GENERIC_PARAMETER:
                    return GENERIC_PARAMETER;
                case CLASS:
                case INTERFACE:
                    return FIELD;
                default:
                    throw new AnnotationProcessorException(element,"Cannot find variable type element real type");
            }
        }

        throw new AnnotationProcessorException(element,"Unmanaged Type of element");
    }

    public static AnnotatedInfo getInfoOf(Element element) throws AnnotationProcessorException{
        switch(getTypeOf(element)){
            case METHOD:
                AbstractClassInfo parentClassInfo = getParentTypeInfo((ExecutableElement)element);
                Preconditions.checkNotNull(parentClassInfo,"Cannot get parent class info of method {}",element);
                return new MethodInfo(parentClassInfo,(ExecutableElement)element);
            case INTERFACE:
            case ANNOTATION:
            case CLASS:
                return AbstractClassInfo.getClassInfo((TypeElement) element);
            case PACKAGE:
                return PackageInfo.getPackageInfo((PackageElement) element);
            case FIELD:
                return new FieldInfo((ClassInfo)getParentTypeInfo((VariableElement)element),(VariableElement)element);
            default:
                throw new AnnotationProcessorException(element,"Cannot build info for given element");
        }
    }

    private static TypeElement getParentTypeElement(Element element) {
        Element parentElement = element.getEnclosingElement();
        while(parentElement!=null){
            if(parentElement instanceof TypeElement){
                return (TypeElement)parentElement;
            }
            parentElement = parentElement.getEnclosingElement();
        }
        return null;
    }

    private static AbstractClassInfo getParentTypeInfo(ExecutableElement element) throws AnnotationProcessorException{
        TypeElement parentTypeElement = getParentTypeElement(element);
        Preconditions.checkNotNull(parentTypeElement,"Cannot get the parent class of method {}",element);
        return AbstractClassInfo.getClassInfo(parentTypeElement);
    }

    private static AbstractClassInfo getParentTypeInfo(VariableElement element) throws AnnotationProcessorException{
        TypeElement parentTypeElement = getParentTypeElement(element);
        Preconditions.checkNotNull(parentTypeElement,"Cannot get the parent class of variable {}",element);
        return AbstractClassInfo.getClassInfo(parentTypeElement);
    }

    public static PackageInfo getFirstParentTypeInfo(PackageInfo info){
        Elements elements=CURRENT_ELEMENT_UTILS.get();
        if(elements!=null){
            for(String potentialParentName: ClassUtils.getPotentialParentPackageNameList(info.getName())) {
                try {
                    PackageElement packageElement = elements.getPackageElement(potentialParentName);

                    if (packageElement != null) {
                        return PackageInfo.getPackageInfo(packageElement);
                    }
                }
                catch(NullPointerException e){
                    //LOG.debug("Error on <{}>",potentialParentName);
                }
            }
        }
        return null;
    }
}
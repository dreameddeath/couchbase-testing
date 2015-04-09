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

package com.dreameddeath.core.tools.annotation.processor;

import com.dreameddeath.core.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.core.tools.annotation.processor.reflection.*;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;

public enum AnnotationElementType {
    PACKAGE,
    CLASS,
    INTERFACE,
    METHOD,
    FIELD,
    METHOD_PARAMETER,
    GENERIC_PARAMETER;


    public static ThreadLocal<Elements> CURRENT_ELEMENT_UTILS=new ThreadLocal<>();

    public static AnnotationElementType getTypeOf(Element element) throws AnnotationProcessorException {
        if(element instanceof PackageElement){
            return PACKAGE;
        }
        else if(element instanceof TypeParameterElement){
            return GENERIC_PARAMETER;
        }
        else if(element instanceof TypeElement){
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
                return new MethodInfo(getParentTypeInfo((ExecutableElement)element),(ExecutableElement)element);
            case INTERFACE:
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
        return AbstractClassInfo.getClassInfo(getParentTypeElement(element));
    }

    private static AbstractClassInfo getParentTypeInfo(VariableElement element) throws AnnotationProcessorException{
        return AbstractClassInfo.getClassInfo(getParentTypeElement(element));
    }
}
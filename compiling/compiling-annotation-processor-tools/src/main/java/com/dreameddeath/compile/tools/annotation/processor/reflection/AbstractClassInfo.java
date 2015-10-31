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

import com.dreameddeath.compile.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 15/03/2015.
 */
public abstract class AbstractClassInfo extends AnnotatedInfo {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractClassInfo.class);
    private static Map<TypeElement,AbstractClassInfo> typeElementToInfoMap = new HashMap<>();
    private static Map<Class,AbstractClassInfo> classToInfoMap = new HashMap<>();


    public static <T extends Annotation> AbstractClassInfo getClassInfoFromAnnot(T annot,AnnotGetter<T> getter){
        try{
            Class<?> clazz = getter.get(annot);
            if(clazz==null){
                return null;
            }
            else {
                return getClassInfo(getter.get(annot));
            }
        }
        catch(MirroredTypeException e){
            return AbstractClassInfo.getClassInfo((TypeElement)((DeclaredType) e.getTypeMirror()).asElement());
        }
    }


    public static AbstractClassInfo getClassInfo(String name) throws ClassNotFoundException{
        if(AnnotationElementType.CURRENT_ELEMENT_UTILS.get()!=null){
            try {
                TypeElement elt = AnnotationElementType.CURRENT_ELEMENT_UTILS.get().getTypeElement(name);
                if (elt != null) {
                    return getClassInfo(elt);
                }
            }
            catch (Throwable e){
                //Ignore error to fallback to class
            }
        }
        return getClassInfo(Thread.currentThread().getContextClassLoader().loadClass(name));
    }


    public static AbstractClassInfo getClassInfo(Class clazz){
        synchronized (classToInfoMap) {
            if (!classToInfoMap.containsKey(clazz)) {
                if (clazz.isAnnotation()) {
                    return new AnnotationInfo(clazz);
                }
                else if (clazz.isInterface()) {
                    return new InterfaceInfo(clazz);
                }
                else {
                    return new ClassInfo(clazz);
                }
            }
            return classToInfoMap.get(clazz);
        }
    }

    public static AbstractClassInfo getClassInfo(TypeElement elt){
        synchronized (typeElementToInfoMap) {
            if (!typeElementToInfoMap.containsKey(elt)) {
                //if(elt.getKind().)
                if (elt.getKind().equals(ElementKind.ANNOTATION_TYPE)) {
                    return new AnnotationInfo(elt);
                }
                else if (elt.getKind().isInterface()) {
                    return new InterfaceInfo(elt);
                }
                else {
                    return new ClassInfo(elt);
                }
            }
            return typeElementToInfoMap.get(elt);
        }
    }


    private static String getClassName(TypeElement element) {
        Element currElement = element;
        String result = element.getSimpleName().toString();
        while (currElement.getEnclosingElement() != null) {
            currElement = currElement.getEnclosingElement();
            if (currElement instanceof TypeElement) {
                result = currElement.getSimpleName() + "$" + result;
            } else if (currElement instanceof PackageElement) {
                if (!"".equals(currElement.getSimpleName())) {
                    result = ((PackageElement) currElement).getQualifiedName() + "." + result;
                }
            }
        }
        return result;
    }

    private static Class getClassFrom(TypeElement element) {
        try{
            return Thread.currentThread().getContextClassLoader().loadClass(getClassName(element));
        }
        catch (ClassNotFoundException e) {
            //System.out.println(e);
        }
        try {
            return Class.forName(getClassName(element));
        }
        catch (ClassNotFoundException e) {
            //LOG.warn("Cannot find class for ")
            //System.out.println(e);
        }
        catch(Throwable e){
            LOG.error("Unexpected error ",e);
        }
        return null;
    }

    private static Class getClassFrom(TypeMirror type) {
        if (type instanceof DeclaredType) {
            if (((DeclaredType) type).asElement() instanceof TypeElement) {
                return getClassFrom((TypeElement) ((DeclaredType) type).asElement());
            }
        }
        return null;
    }

    private PackageInfo packageInfo;
    private AbstractClassInfo enclosingClass=null;
    private String simpleName;
    private String fullName;
    private String compiledFileName;
    private Class<?> clazz = null;
    private DeclaredType declaredType = null;
    private TypeElement typeElement = null;
    private List<InterfaceInfo> parentInterfaces = null;
    private List<MethodInfo> declaredMethods = null;
    private List<ParameterizedTypeInfo> parameterizedTypeInfos=new ArrayList<>();

    public abstract boolean isInterface();

    private void init(){
        if(typeElement!=null){
            typeElementToInfoMap.put(typeElement,this);
        }
        if(clazz!=null){
            classToInfoMap.put(clazz,this);
        }
        if(typeElement!=null){
            if(!(typeElement.getEnclosingElement() instanceof PackageElement)){
                enclosingClass = AbstractClassInfo.getClassInfo((TypeElement)typeElement.getEnclosingElement());
            }
            packageInfo = PackageInfo.getPackageInfo(typeElement);
            simpleName = typeElement.getSimpleName().toString();
            //Manage $ for inner clazz
            compiledFileName = typeElement.getQualifiedName().toString();
            fullName = packageInfo.getName()+"."+typeElement.getQualifiedName().toString().substring(packageInfo.getName().length() + 1).replace(".","$");
            for(TypeParameterElement parameterElement:typeElement.getTypeParameters()){
                parameterizedTypeInfos.add(new ParameterizedTypeInfo(parameterElement));
            }
        }
        else if(clazz!=null){
            packageInfo = PackageInfo.getPackageInfo(clazz.getPackage());
            if(clazz.getEnclosingClass()!=null){
                enclosingClass = AbstractClassInfo.getClassInfo(clazz.getEnclosingClass());
            }
            simpleName = clazz.getSimpleName();
            fullName = clazz.getName();
            compiledFileName = (enclosingClass!=null)?enclosingClass.compiledFileName+"$"+clazz.getSimpleName():clazz.getName();
            for(TypeVariable param:clazz.getTypeParameters()){
                parameterizedTypeInfos.add(new ParameterizedTypeInfo(param));
            }
        }
        else{
            //TODO throw exception
        }
    }

    public AbstractClassInfo(TypeElement element){
        super(element);
        typeElement = element;
        declaredType = (DeclaredType)element.asType();
        clazz = getClassFrom(declaredType);
        init();
    }

    public AbstractClassInfo(Class<?> clazz){
        super(clazz);
        this.clazz = clazz;
        init();
    }

    public List<InterfaceInfo> getParentInterfaces(){
        if(parentInterfaces==null) {
            parentInterfaces = new ArrayList<>();
            if (clazz != null) {
                for (Class<?> interfaceClass : clazz.getInterfaces()) {
                    parentInterfaces.add((InterfaceInfo)AbstractClassInfo.getClassInfo(interfaceClass));
                }
            }
            else {
                for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                    TypeElement interfaceElement = (TypeElement) ((DeclaredType) interfaceType).asElement();
                    parentInterfaces.add(new InterfaceInfo(interfaceElement));
                }
            }
        }
        return parentInterfaces;
    }

    public List<MethodInfo> getDeclaredMethods(){
        if(declaredMethods==null){
            declaredMethods=new ArrayList<>();
            if(clazz!=null){
                for(Method declaredMethod : clazz.getDeclaredMethods()){
                    declaredMethods.add(new MethodInfo(this,declaredMethod));
                }
            }
            else{
                for(Element elt:typeElement.getEnclosedElements()){
                    try {
                        AnnotationElementType eltType = AnnotationElementType.getTypeOf(elt);
                        if(eltType.equals(AnnotationElementType.METHOD)){
                            declaredMethods.add(new MethodInfo(this,(ExecutableElement) elt));
                        }
                    }
                    catch(AnnotationProcessorException e){
                        throw new RuntimeException("Unhandled element",e);
                    }
                }
            }
        }
        return Collections.unmodifiableList(declaredMethods);
    }

    public MethodInfo getDeclaredMethod(String name,ParameterizedTypeInfo ... infos){
        MethodInfo foundMethod=null;
        for(MethodInfo methodInfo:getDeclaredMethods()){
            if(methodInfo.getName().equals(name) && (methodInfo.getMethodParameters().size()==infos.length)){
                Iterator<ParameterizedTypeInfo> iterator= methodInfo.getMethodParameters().iterator();
                foundMethod = methodInfo;
                for(ParameterizedTypeInfo param:infos){
                    if(iterator.hasNext()){
                        ParameterizedTypeInfo refParam=iterator.next();
                        if(!refParam.getMainType().isAssignableFrom(param.getMainType())){
                            foundMethod=null;
                            break;
                        }
                    }
                    else{
                        foundMethod=null;
                        break;
                    }
                }
                if(foundMethod!=null){
                    break;
                }
            }
        }
        return foundMethod;
    }

    public MethodInfo getMethod(String name,ParameterizedTypeInfo ...infos){
        MethodInfo foundMethod = getDeclaredMethod(name,infos);
        if(foundMethod==null){
            for(InterfaceInfo parentInterface:getParentInterfaces()){
                foundMethod = parentInterface.getMethod(name,infos);
                if(foundMethod!=null) break;
            }
        }
        return foundMethod;
    }

    public String getSimpleName(){
        return simpleName;
    }

    public String getFullName(){
        return fullName;
    }

    public String getCompiledFileName() {
        return compiledFileName;
    }

    public String getImportName(){
        return getFullName().replace("$",".");
    }

    public String getName(){
        return fullName;
    }

    public TypeElement getTypeElement(){
        return typeElement;
    }

    public Class getCurrentClass(){
        return clazz;
    }

    public boolean isInstanceOf(Class clazz){
        return AbstractClassInfo.getClassInfo(clazz).isAssignableFrom(this);
    }


    public boolean isAssignableFrom(Class clazz){
        return isAssignableFrom(AbstractClassInfo.getClassInfo(clazz));
    }

    public boolean isAssignableFrom(TypeElement elt){
        return isAssignableFrom(AbstractClassInfo.getClassInfo(elt));
    }

    public boolean isAssignableFrom(AbstractClassInfo target){
        if((clazz!=null) && (target.clazz!=null)){
            return (clazz==target.clazz) || clazz.isAssignableFrom(target.clazz);
        }
        else {
            ClassInfo targetSuperClass=null;
            if((target instanceof ClassInfo)){
                targetSuperClass = ((ClassInfo)target).getSuperClass();
            }

            //check superclass recursively
            if((targetSuperClass!=null) && this.isAssignableFrom(targetSuperClass)){
                return true;
            }
            //Compare names
            if(getFullName().equals(target.getFullName())){
                return true;
            }
            //Check using targetClass Interfaces
            else{
                for(AbstractClassInfo parentInterfaceClassInfo:target.getParentInterfaces()){
                    if(this.isAssignableFrom(parentInterfaceClassInfo)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public AbstractClassInfo getEnclosingClass() {
        return enclosingClass;
    }

    @Override
    public boolean equals(Object o){
        if(this==o){
            return true;
        }
        else if( ! (o instanceof AbstractClassInfo)){
            return false;
        }
        else{
            AbstractClassInfo target = (AbstractClassInfo)o;
            return this.getFullName().equals(target.getFullName());
        }
    }

    @Override
    public int hashCode(){
        return (this.clazz!=null)?clazz.hashCode():typeElement.getQualifiedName().toString().hashCode();
    }
}

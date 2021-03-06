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

import com.dreameddeath.compile.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationElementType;
import com.squareup.javapoet.ClassName;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 15/03/2015.
 */
public abstract class AbstractClassInfo extends AnnotatedInfo {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractClassInfo.class);
    private static final Map<TypeElement,AbstractClassInfo> typeElementToInfoMap = new HashMap<>();
    private static final Map<Class,AbstractClassInfo> classToInfoMap = new HashMap<>();


    public static <T extends Annotation> AbstractClassInfo getClassInfoFromAnnot(T annot,AnnotGetter<T> getter){
        try{
            Class<?> clazz = getter.get(annot);
            if(clazz==null){
                return null;
            }
            else {
                return getClassInfo(clazz);
            }
        }
        catch(MirroredTypeException e){
            return AbstractClassInfo.getClassInfo((TypeElement)((DeclaredType) e.getTypeMirror()).asElement());
        }
    }

    public static <T extends Annotation> AbstractClassInfo[] getClassInfoFromAnnot(T annot,AnnotArrayGetter<T> getter){
        AbstractClassInfo[] abstractClassInfos;
        try{
            Class[] clazzArray = getter.get(annot);
            if(clazzArray==null){
                return null;
            }
            else {
                abstractClassInfos = new AbstractClassInfo[clazzArray.length + 1];
                for(int i=0;i<clazzArray.length;++i) {
                    try {
                        abstractClassInfos[i] = getClassInfo(clazzArray[i]);
                    } catch (MirroredTypeException e) {
                        abstractClassInfos[i] = AbstractClassInfo.getClassInfo((TypeElement) ((DeclaredType) e.getTypeMirror()).asElement());
                    }
                }
            }
        }
        catch(MirroredTypesException e){
            abstractClassInfos=new AbstractClassInfo[e.getTypeMirrors().size()];
            for(int i=0;i<e.getTypeMirrors().size();++i){
                abstractClassInfos[i]=AbstractClassInfo.getClassInfo((TypeElement)((DeclaredType) e.getTypeMirrors().get(i)).asElement());
            }
        }
        return abstractClassInfos;
    }


    public static AbstractClassInfo getClassInfo(String name) throws ClassNotFoundException{
        if(AnnotationElementType.CURRENT_ELEMENT_UTILS.get()!=null){
            try {
                String elementName = name.replaceAll("\\$",".");
                TypeElement elt = AnnotationElementType.CURRENT_ELEMENT_UTILS.get().getTypeElement(elementName);
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
                if (!"".equals(currElement.getSimpleName().toString())) {
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
    private boolean isBaseType;
    private Class<?> clazz = null;
    private DeclaredType declaredType = null;
    private TypeElement typeElement = null;
    private List<InterfaceInfo> parentInterfaces = null;
    private List<ParameterizedTypeInfo> parameterizedInterfaceTypeInfos = null;
    private List<MethodInfo> declaredMethods = null;
    private List<ParameterizedTypeInfo> parameterizedTypeInfos=new ArrayList<>();
    private ClassName className;
    public abstract boolean isInterface();

    private void init(){
        if(typeElement!=null){
            synchronized (typeElementToInfoMap) {
                typeElementToInfoMap.put(typeElement, this);
            }
        }
        if(clazz!=null){
            synchronized (classToInfoMap) {
                classToInfoMap.put(clazz, this);
            }
        }
        if(typeElement!=null){
            if(typeElement.getEnclosingElement() instanceof TypeElement){
                enclosingClass = AbstractClassInfo.getClassInfo((TypeElement)typeElement.getEnclosingElement());
            }
            else if(!(typeElement.getEnclosingElement() instanceof PackageElement)){
                throw new RuntimeException("The type <"+typeElement.getQualifiedName().toString()+"> isn't a well declared class/interface because the enclosing element <"+typeElement.getEnclosingElement()+"> isn't a package type nor a parent class");
            }
            packageInfo = PackageInfo.getPackageInfo(typeElement);
            simpleName = typeElement.getSimpleName().toString();
            if(!typeElement.asType().getKind().isPrimitive()) {
                className = ClassName.get(typeElement);
            }
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
            if(!clazz.isPrimitive() && !clazz.isArray()) {
                className = ClassName.get(clazz);
            }
            compiledFileName = (enclosingClass!=null)?enclosingClass.compiledFileName+"$"+clazz.getSimpleName():clazz.getName();
            for(TypeVariable param:clazz.getTypeParameters()){
                parameterizedTypeInfos.add(new ParameterizedTypeInfo(param));
            }
        }
        else{
            //TODO throw exception
        }

        //manage isBaseType
        this.isBaseType =
                AbstractClassInfo.getClassInfo(String.class).isAssignableFrom(this)
                || AbstractClassInfo.getClassInfo(Boolean.class).isAssignableFrom(this)
                || AbstractClassInfo.getClassInfo(DateTime.class).isAssignableFrom(this)
                || AbstractClassInfo.getClassInfo(UUID.class).isAssignableFrom(this)
                || AbstractClassInfo.getClassInfo(Number.class).isAssignableFrom(this)
                || AbstractClassInfo.getClassInfo(Map.class).isAssignableFrom(this)
                || AbstractClassInfo.getClassInfo(Collection.class).isAssignableFrom(this);
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

    public boolean isBaseType() {
        return isBaseType;
    }

    public ClassName getClassName() {
        return className;
    }

    public static AbstractClassInfo getEffectiveGenericType(Class clazz,Class parameterizedClass, int pos) {
        return getEffectiveGenericType(AbstractClassInfo.getClassInfo(clazz),AbstractClassInfo.getClassInfo(parameterizedClass),0);
    }

    public static AbstractClassInfo getEffectiveGenericType(AbstractClassInfo clazz,AbstractClassInfo parameterizedClass, int pos) {
        Deque<AbstractClassInfo> stack = getAncestorsPath(clazz,parameterizedClass);
        if(stack.size()==0){
            throw new RuntimeException("Cannot find path from class "+clazz.getName()+" to ancestor class "+parameterizedClass.getName());
        }
        else if(stack.size()==1){
            throw new RuntimeException("Ancestor and ref class are both the same");
        }
        AbstractClassInfo currParent=stack.pollLast();
        Iterator<AbstractClassInfo> iterator=stack.descendingIterator();
        int currParentParameterPos=pos;
        while(iterator.hasNext()){
            AbstractClassInfo currChild = iterator.next();
            ParameterizedTypeInfo parentGenericType = null;
            //Everything is a class
            if(currParent!=null && !currParent.isInterface()){
                parentGenericType = ((ClassInfo)currChild).getParameterizedSuperClass();
            }
            else if(currChild.isInterface()){
                parentGenericType = currChild.getParentParameterizedInterfaces().get(0);
            }
            //Parent is interface
            else {
                int interfacePos=0;
                for(InterfaceInfo interfaceClass:currChild.getParentInterfaces()){
                    if(interfaceClass.equals(currParent)){
                        parentGenericType = currChild.getParentParameterizedInterfaces().get(interfacePos);
                        break;
                    }
                    interfacePos++;
                }
            }

            if(parentGenericType!=null){
                ParameterizedTypeInfo type=parentGenericType.getMainTypeGeneric(currParentParameterPos);
                if(type.getTypeParamName()==null){
                    return type.getMainType();
                }
                else{
                    String typeName =type.getTypeParamName();
                    int currChildParamPos = 0;
                    for(ParameterizedTypeInfo currTypeParam:currChild.getParameterizedTypeInfos()){
                        if(currTypeParam.getTypeParamName().equals(typeName)){
                            break;
                        }
                        currChildParamPos++;
                    }
                    currParentParameterPos = currChildParamPos;
                }
            }
            currParent = currChild;
        }
        return null;
    }


    public static Deque<AbstractClassInfo> getAncestorsPath(AbstractClassInfo currentClass, AbstractClassInfo ancestorClass){
        LinkedList<AbstractClassInfo> stack = new LinkedList<>();
        boolean found=false;
        while(!found){
            stack.add(currentClass);
            if(currentClass.equals(ancestorClass)){
                found=true;
                continue;
            }
            if(ancestorClass.isInterface()){
                for(InterfaceInfo interfaceClass:currentClass.getParentInterfaces()){
                    Deque<AbstractClassInfo> pathInterface=getAncestorsPath(interfaceClass,ancestorClass);
                    if(pathInterface.size()!=0){
                        stack.addAll(pathInterface);
                        found=true;
                        break;
                    }
                }
                if(found){
                    continue;
                }
            }
            if(currentClass.isInterface()) {
                break;
            }
            else {
                currentClass = ((ClassInfo)currentClass).getSuperClass();
                if ((currentClass == null) || (Object.class.equals(currentClass.getCurrentClass()))) {
                    break;
                }
            }
        }
        if(found){
            return stack;
        }
        else{
            stack.clear();
            return stack;
        }
    }

    public List<ParameterizedTypeInfo> getParentParameterizedInterfaces() {
        if(parameterizedInterfaceTypeInfos==null) {
            parameterizedInterfaceTypeInfos = new ArrayList<>();
            if (clazz != null) {
                for (Type interfaceParameterizedType : clazz.getGenericInterfaces()) {
                    parameterizedInterfaceTypeInfos.add(ParameterizedTypeInfo.getParameterizedTypeInfo(interfaceParameterizedType));
                }
            }
            else {
                for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                    parameterizedInterfaceTypeInfos.add(ParameterizedTypeInfo.getParameterizedTypeInfo(interfaceType));
                }
            }
        }
        return parameterizedInterfaceTypeInfos;
    }

    public List<ParameterizedTypeInfo> getParameterizedTypeInfos(){
        return Collections.unmodifiableList(parameterizedTypeInfos);
    }
}

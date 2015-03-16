package com.dreameddeath.core.tools.annotation.processor.reflection;

import com.dreameddeath.core.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.core.tools.annotation.processor.AnnotationElementType;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * Created by CEAJ8230 on 15/03/2015.
 */
public abstract class AbstractClassInfo extends AnnotatedInfo {
    private static Map<TypeElement,AbstractClassInfo> _typeElementToInfoMap = new HashMap<>();
    private static Map<Class,AbstractClassInfo> _classToInfoMap = new HashMap<>();


    public static <T extends Annotation> AbstractClassInfo getClassInfoFromAnnot(T annot,AnnotGetter<T> getter){
        try{
            return AbstractClassInfo.getClassInfo(getter.get(annot));
        }
        catch(MirroredTypeException e){
            return AbstractClassInfo.getClassInfo((TypeElement)((DeclaredType) e.getTypeMirror()).asElement());
        }
    }


    public static AbstractClassInfo getClassInfo(Class clazz){
        if(!_classToInfoMap.containsKey(clazz)){
            if(clazz.isInterface()){
                return new InterfaceInfo(clazz);
            }
            else{
                return new ClassInfo(clazz);
            }
        }
        return _classToInfoMap.get(clazz);
    }

    public static AbstractClassInfo getClassInfo(TypeElement elt){
        if(!_typeElementToInfoMap.containsKey(elt)){
            if(elt.getKind().isInterface()){
                return new InterfaceInfo(elt);
            }
            else{
                return new ClassInfo(elt);
            }
        }
        return _typeElementToInfoMap.get(elt);
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
        try {
            return Class.forName(getClassName(element));
        } catch (Exception e) {
            //System.out.println(e);
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

    private PackageInfo _packageInfo;
    private String _simpleName;
    private String _fullName;
    private Class<?> _class = null;
    private DeclaredType _declaredType = null;
    private TypeElement _typeElement = null;
    private List<InterfaceInfo> _parentInterfaces = null;
    private List<MethodInfo> _declaredMethods = null;
    private List<ParameterizedTypeInfo> _parameterizedTypeInfos=new ArrayList<>();

    public abstract boolean isInterface();

    private void init(){
        if(_typeElement!=null){
            _typeElementToInfoMap.put(_typeElement,this);
        }
        if(_class!=null){
            _classToInfoMap.put(_class,this);
        }
        if(_typeElement!=null){
            _packageInfo = PackageInfo.getPackageInfo(_typeElement);
            _simpleName = _typeElement.getSimpleName().toString();
            //Manage $ for inner class
            _fullName = _packageInfo.getName()+"."+_typeElement.getQualifiedName().toString().substring(_packageInfo.getName().length() + 1).replace(".","$");
            for(TypeParameterElement parameterElement:_typeElement.getTypeParameters()){
                _parameterizedTypeInfos.add(new ParameterizedTypeInfo(parameterElement));
            }
        }
        else{
            _packageInfo = PackageInfo.getPackageInfo(_class.getPackage());
            _simpleName = _class.getSimpleName();
            _fullName = _class.getName();
            for(TypeVariable param:_class.getTypeParameters()){
                _parameterizedTypeInfos.add(new ParameterizedTypeInfo(param));
            }
        }
    }

    public AbstractClassInfo(TypeElement element){
        super(element);
        _typeElement = element;
        _declaredType = (DeclaredType)element.asType();
        _class = getClassFrom(_declaredType);
        init();
    }

    public AbstractClassInfo(Class<?> clazz){
        super(clazz);
        _class = clazz;
        init();
    }

    public List<InterfaceInfo> getParentInterfaces(){
        if(_parentInterfaces==null) {
            _parentInterfaces = new ArrayList<>();
            if (_class != null) {
                for (Class<?> interfaceClass : _class.getInterfaces()) {
                    _parentInterfaces.add((InterfaceInfo)AbstractClassInfo.getClassInfo(interfaceClass));
                }
            }
            else {
                for (TypeMirror interfaceType : _typeElement.getInterfaces()) {
                    TypeElement interfaceElement = (TypeElement) ((DeclaredType) interfaceType).asElement();
                    _parentInterfaces.add(new InterfaceInfo(interfaceElement));
                }
            }
        }
        return _parentInterfaces;
    }

    public List<MethodInfo> getDeclaredMethods(){
        if(_declaredMethods==null){
            _declaredMethods=new ArrayList<>();
            if(_class!=null){
                for(Method declaredMethod : _class.getDeclaredMethods()){
                    _declaredMethods.add(new MethodInfo(this,declaredMethod));
                }
            }
            else{
                for(Element elt:_typeElement.getEnclosedElements()){
                    try {
                        AnnotationElementType eltType = AnnotationElementType.getTypeOf(elt);
                        if(eltType.equals(AnnotationElementType.METHOD)){
                            _declaredMethods.add(new MethodInfo(this,(ExecutableElement) elt));
                        }
                    }
                    catch(AnnotationProcessorException e){
                        throw new RuntimeException("Unhandled element",e);
                    }
                }
            }
        }
        return Collections.unmodifiableList(_declaredMethods);
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

    public String getSimpleName(){
        return _simpleName;
    }

    public String getFullName(){
        return _fullName;
    }

    public String getName(){
        return _fullName;
    }

    public TypeElement getTypeElement(){
        return _typeElement;
    }

    public Class getCurrentClass(){
        return _class;
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
        if((_class!=null) && (target._class!=null)){
            return _class.isAssignableFrom(target._class);
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
        return _packageInfo;
    }
}

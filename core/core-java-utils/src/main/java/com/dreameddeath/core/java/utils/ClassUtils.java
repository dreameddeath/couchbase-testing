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

package com.dreameddeath.core.java.utils;


import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 20/10/2015.
 */
public class ClassUtils {
    public static Class getCallerClass(int level) throws ClassNotFoundException {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String rawFQN = stElements[level+1].toString().split("\\(")[0];
        return Thread.currentThread().getContextClassLoader().loadClass(rawFQN.substring(0, rawFQN.lastIndexOf('.')));
    }

    public static Deque<Class> getAncestorsPath(Class currentClass, Class ancestorClass){
        LinkedList<Class> stack = new LinkedList<>();
        boolean found=false;
        while(!found){
            stack.add(currentClass);
            if(currentClass.equals(ancestorClass)){
                found=true;
                break;
            }
            if(ancestorClass.isInterface()){
                for(Class interfaceClass:currentClass.getInterfaces()){
                    Deque<Class> pathInterface=getAncestorsPath(interfaceClass,ancestorClass);
                    if(pathInterface.size()!=0){
                        stack.addAll(pathInterface);
                        found=true;
                        break;
                    }
                }
                if(found){
                    break;
                }
            }
            if(currentClass.isInterface()) {
                break;
            }
            currentClass = currentClass.getSuperclass();
            if ((currentClass == null) || currentClass.equals(Object.class)) {
                break;
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

    public static Class getEffectiveGenericType(Class clazz,Class parameterizedClass, int pos){
        Deque<Class> stack = getAncestorsPath(clazz,parameterizedClass);
        if(stack.size()==0){
            throw new RuntimeException("Cannot find path from class "+clazz.getName()+" to ancestor class "+parameterizedClass.getName());
        }
        else if(stack.size()==1){
            throw new RuntimeException("Ancestor and ref class are both the same");
        }
        Class currParent=stack.pollLast();
        int currParentParameterPos = pos;
        Iterator<Class> iterator=stack.descendingIterator();
        while(iterator.hasNext()){
            Class currChild = iterator.next();
            Type parentGenericType = null;
            //Everything is a class
            if(!currParent.isInterface()){
                parentGenericType = currChild.getGenericSuperclass();
            }
            else if(currChild.isInterface()){
                parentGenericType = currChild.getGenericInterfaces()[0];
            }
            //Parent is interface
            else {
                int interfacePos=0;
                for(Class interfaceClass:currChild.getInterfaces()){
                    if(interfaceClass.equals(currParent)){
                        parentGenericType = currChild.getGenericInterfaces()[interfacePos];
                        break;
                    }
                    interfacePos++;
                }
            }

            if(!(parentGenericType instanceof ParameterizedType)){
                ///TODO throw an error
            }
            else{
                Type type=((ParameterizedType)parentGenericType).getActualTypeArguments()[currParentParameterPos];
                if(type instanceof Class){
                    return (Class)type;
                }
                else if(type instanceof TypeVariable){
                    String typeName = ((TypeVariable)type).getName();
                    int currChildParamPos = 0;
                    for(Type currTypeParam:currChild.getTypeParameters()){
                        if(currTypeParam instanceof TypeVariable){
                            if(((TypeVariable)currTypeParam).getName().equals(typeName)){
                                break;
                            }
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

    public static Package getFirstParentPackage(Package pkg){
        if(!pkg.getName().contains(".")){
            return null;
        }
        for(String potentialParent :getPotentialParentPackageNameList(pkg.getName())) {
            Package parent = Package.getPackage(potentialParent);
            if(parent==null){
                try{
                    Thread.currentThread().getContextClassLoader().loadClass(potentialParent+".package-info");
                    parent = Package.getPackage(potentialParent);
                }
                catch(ClassNotFoundException e){
                    //ignore
                }
            }
            if(parent!=null){
                return parent;
            }
        }
        return null;
    }

    public static List<String> getPotentialParentPackageNameList(String name){
        String[] parts=name.split("\\.");
        List<String> potentialParents = new ArrayList<>(parts.length);
        if(parts.length>1) {
            StringBuilder fullName = new StringBuilder(parts[0]);
            potentialParents.add(fullName.toString());
            for (int pos = 1; pos < (parts.length - 1); ++pos) {
                fullName.append('.').append(parts[pos]);
                potentialParents.add(0, fullName.toString());
            }
        }
        return potentialParents;
    }

    public static <A extends Annotation> Class getClassWithAnnotation(Class objectClass,Class<A> annotation) {
        if(objectClass.getAnnotation(annotation)!=null){
            return objectClass;
        }
        for(Class implementedInterface:objectClass.getInterfaces()){
          if(implementedInterface.getAnnotation(annotation)!=null){
              return implementedInterface;
          }
        }
        if(objectClass.getSuperclass()!=null){
            return getClassWithAnnotation(objectClass,annotation);
        }
        return null;
    }
}

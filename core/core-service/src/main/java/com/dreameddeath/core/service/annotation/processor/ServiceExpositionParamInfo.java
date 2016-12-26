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

package com.dreameddeath.core.service.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.reflection.*;
import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 08/04/2015.
 */
public class ServiceExpositionParamInfo {
    private final static String MAIN_PATTERN_STR = "(\\w+)(?:\\s*(<?=>?)\\s*(\\w+(?:.\\w+(?:\\(\\))?)*))?";
    private final static Pattern PATH_PATTERN = Pattern.compile(":" + MAIN_PATTERN_STR);
    private final static Pattern QUERY_PATTERN = Pattern.compile(MAIN_PATTERN_STR);
    private final boolean isQuery;
    private String name;
    private Direction mappingDirection;
    private String getterString;
    private String setterString;
    private ParameterizedTypeInfo typeInfo;

    public enum Direction {
        TO_PATH_ONLY,
        BIDIRECTIONNAL
    }

    private void initFromAttributePath(String name,String attributePath,MethodInfo methodInfo){
        this.name = name;
        //;
        if(attributePath.equals("") || !attributePath.contains(".")){
            getterString = attributePath;
            setterString = attributePath+"="+name;
            typeInfo = methodInfo.getMethodParamByName(attributePath);
        }
        else{
            String[] attributeElements =attributePath.split("\\.");
            getterString = "";
            setterString = "";
            AbstractClassInfo currentElement = null;
            int nbProcessedElement = 0;
            for(String attributeElement : attributeElements){
                nbProcessedElement++;
                if(nbProcessedElement == 1){
                    ParameterizedTypeInfo foundMethod=methodInfo.getMethodParamByName(attributeElement);
                    if(foundMethod==null){
                        throw new RuntimeException("Cannot find method attribute <"+attributeElement+"> from method <"+methodInfo.getFullName()+">");
                    }
                    currentElement = foundMethod.getMainType();
                    getterString = attributeElement;
                    setterString = attributeElement;
                }
                else{
                    //First try to access by getter/Setter
                    String attributeMethodSuffix = attributeElement.substring(0, 1).toUpperCase() + attributeElement.substring(1);
                    FieldInfo fieldInfo=null;
                    Preconditions.checkNotNull(currentElement,"The attribute element %s doesn't have matching current value within atrribute %s and path %s",attributeElement,name,attributePath);
                    MethodInfo getterMethod = currentElement.getMethod("get" + attributeMethodSuffix);
                    MethodInfo setterMethod = null;
                    if (getterMethod != null) {
                        setterMethod = currentElement.getMethod("set" + attributeMethodSuffix, getterMethod.getReturnType());
                        if (setterMethod == null) {
                            throw new RuntimeException("Cannot find setter for " + attributeElement + " in class " + currentElement.getFullName());
                        }
                    } else if (currentElement instanceof ClassInfo) {
                        for (String lookupName : new String[]{attributeElement, "_" + attributeElement}) {
                            fieldInfo = ((ClassInfo) currentElement).getFieldByName(lookupName);
                            if (fieldInfo != null) {
                                setterMethod = currentElement.getMethod("set" + attributeMethodSuffix, fieldInfo.getType());
                                break;
                            } else {
                                throw new RuntimeException("Cannot find getter " + attributeElement + " for class " + currentElement.getFullName());
                            }
                        }
                    }

                    if (nbProcessedElement < attributeElements.length) {
                        if (getterMethod != null) {
                            getterString += "." + getterMethod.getName() + "()";
                        } else {
                            Preconditions.checkNotNull(fieldInfo,"neither getter method no field info found for attribute %s",name);
                            getterString += "." + fieldInfo.getName();
                        }
                    } else {
                        if (getterMethod != null) {
                            typeInfo = getterMethod.getReturnType();
                            setterString = getterString + "." + setterMethod.getName() + "(" + name + ")";
                            getterString += getterString + "." + getterMethod.getName() + "()";
                        } else {
                            Preconditions.checkNotNull(fieldInfo,"neither getter method no field info found for attribute %s",name);
                            setterString = getterString + "." + fieldInfo.getName() + "=" + name;
                            getterString += "." + fieldInfo.getName();
                            typeInfo = fieldInfo.getType();
                        }
                    }
                }
            }
        }

        if(typeInfo==null){
            throw new RuntimeException("Cannot find type for info "+attributePath+ " in method "+methodInfo.getFullName());
        }
    }

    public ServiceExpositionParamInfo(String name,String paramPath, MethodInfo methodInfo) {
        isQuery = false;
        initFromAttributePath(name,paramPath,methodInfo);
    }

    public ServiceExpositionParamInfo(boolean isQuery, String paramDef, MethodInfo methodInfo) {
        this.isQuery = isQuery;
        Matcher matcher;
        if(isQuery){
            matcher = QUERY_PATTERN.matcher(paramDef);
        }
        else {
            matcher = PATH_PATTERN.matcher(paramDef);
        }

        if (matcher.matches()) {
            if(matcher.group(2)!=null) {
                mappingDirection = matcher.group(2).equals("<=") ? Direction.TO_PATH_ONLY : Direction.BIDIRECTIONNAL;
                initFromAttributePath(matcher.group(1), matcher.group(3), methodInfo);
            }
            else{
                mappingDirection = Direction.BIDIRECTIONNAL;
                initFromAttributePath(matcher.group(1), matcher.group(1), methodInfo);
            }
        }
        else{
            throw new RuntimeException("Cannot parse param definition <"+paramDef+">");
        }
    }

    public String getImportName(){
        return typeInfo.getMainType().getImportName();
    }

    public String getClassName(){
        return typeInfo.getMainType().getSimpleName();
    }

    public boolean isQuery() {
        return isQuery;
    }

    public String getName() {
        return name;
    }

    public Direction getMappingDirection() {
        return mappingDirection;
    }

    public String getGetterString() {
        return getterString;
    }

    public String getSetterString() {
        return setterString;
    }

    public String getPatternFormat(){
        if(typeInfo.getMainType().isInstanceOf(Number.class)){
            for(Class clazz:new Class[]{Float.class, Double.class,BigDecimal.class,}){
                if(typeInfo.getMainType().isInstanceOf(clazz)){
                    return "%f";
                }
            }
            return "%d";
        }
        return "%s";
    }

    public ParameterizedTypeInfo getParamType(){
        return typeInfo;
    }
}

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

package com.dreameddeath.core.service.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.reflection.*;

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
    private final boolean _isQuery;
    private String _name;
    private boolean _isSimpleMapping;
    private Direction _mappingDirection;
    private String _getterString;
    private String _setterString;
    private ParameterizedTypeInfo _typeInfo;

    public enum Direction {
        TO_PATH_ONLY,
        BIDIRECTIONNAL
    }

    private void initFromAttributePath(String name,String attributePath,MethodInfo methodInfo){
        _name = name;
        //;
        if((attributePath == null) || attributePath.equals("") || attributePath.split("\\.").length==1){
            _isSimpleMapping = true;
            _getterString = attributePath;
            _setterString = attributePath+"="+_name;
            _typeInfo = methodInfo.getMethodParamByName(attributePath);
        }
        else{
            String[] attributeElements =attributePath.split("\\.");
            _isSimpleMapping = false;
            _getterString = "";
            _setterString = "";
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
                    _getterString = attributeElement;
                    _setterString = attributeElement;
                }
                else{
                    //First try to access by getter/Setter
                    String attributeMethodSuffix = attributeElement.substring(0, 1).toUpperCase() + attributeElement.substring(1);
                    FieldInfo fieldInfo = null;
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
                            _getterString += "." + getterMethod.getName() + "()";
                        } else {
                            _getterString += "." + fieldInfo.getName();
                        }
                    } else {
                        if (getterMethod != null) {
                            _typeInfo = getterMethod.getReturnType();
                            _setterString = _getterString + "." + setterMethod.getName() + "(" + _name + ")";
                            _getterString += _getterString + "." + getterMethod.getName() + "()";
                        } else {
                            _setterString = _getterString + "." + fieldInfo.getName() + "=" + _name;
                            _getterString += "." + fieldInfo.getName();
                            _typeInfo = fieldInfo.getType();
                        }
                    }
                }
            }
        }

        if(_typeInfo==null){
            throw new RuntimeException("Cannot find type for info "+attributePath+ " in method "+methodInfo.getFullName());
        }
    }

    public ServiceExpositionParamInfo(String name,String paramPath, MethodInfo methodInfo) {
        _isQuery = false;
        initFromAttributePath(name,paramPath,methodInfo);
    }

    public ServiceExpositionParamInfo(boolean isQuery, String paramDef, MethodInfo methodInfo) {
        _isQuery = isQuery;
        Matcher matcher;
        if(_isQuery){
            matcher = QUERY_PATTERN.matcher(paramDef);
        }
        else {
            matcher = PATH_PATTERN.matcher(paramDef);
        }

        if (matcher.matches()) {
            if(matcher.group(2)!=null) {
                _mappingDirection = matcher.group(2).equals("<=") ? Direction.TO_PATH_ONLY : Direction.BIDIRECTIONNAL;
                initFromAttributePath(matcher.group(1), matcher.group(3), methodInfo);
            }
            else{
                _mappingDirection = Direction.BIDIRECTIONNAL;
                initFromAttributePath(matcher.group(1), matcher.group(1), methodInfo);
            }
        }
        else{
            throw new RuntimeException("Cannot parse param definition <"+paramDef+">");
        }
    }

    public String getImportName(){
        return _typeInfo.getMainType().getImportName();
    }

    public String getClassName(){
        return _typeInfo.getMainType().getSimpleName();
    }

    public boolean isQuery() {
        return _isQuery;
    }

    public String getName() {
        return _name;
    }

    public Direction getMappingDirection() {
        return _mappingDirection;
    }

    public String getGetterString() {
        return _getterString;
    }

    public String getSetterString() {
        return _setterString;
    }

    public String getPatternFormat(){
        if(_typeInfo.getMainType().isInstanceOf(Number.class)){
            for(Class clazz:new Class[]{Float.class, Double.class,BigDecimal.class,}){
                if(_typeInfo.getMainType().isInstanceOf(clazz)){
                    return "%f";
                }
            }
            return "%d";
        }
        return "%s";
    }

    public ParameterizedTypeInfo getParamType(){
        return _typeInfo;
    }
}

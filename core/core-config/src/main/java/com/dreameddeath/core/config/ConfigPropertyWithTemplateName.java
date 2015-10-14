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

package com.dreameddeath.core.config;

import com.dreameddeath.core.config.exception.ConfigPropertyTemplateBuildException;
import com.dreameddeath.core.config.exception.ConfigPropertyTemplateConstructorNotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 10/10/2015.
 */
public class ConfigPropertyWithTemplateName<T,PTYPE extends IConfigProperty> {
    private String template;
    private String[] templateParts;
    private IConfigPropertyBuilder<PTYPE> builder;

    private void compileTemplate(String template){
        templateParts=template.split("(?<=\\{\\w{1,255}\\})|(?=\\{\\w+\\})");
        StringBuilder sb =new StringBuilder();
        for(int partPos=0;partPos<templateParts.length;++partPos){
            if(partPos%2==0){
                sb.append(templateParts[partPos]);
            }
            else{
                templateParts[partPos] = templateParts[partPos].substring(1,templateParts[partPos].length()-1);
                sb.append("%s");
            }
        }
        this.template = sb.toString();
    }

    public ConfigPropertyWithTemplateName(Class<PTYPE> clazz, String nameTemplate, final T defaultValue){
        compileTemplate(nameTemplate);
        try {
            final Constructor<PTYPE> constructor = clazz.getConstructor(String.class, defaultValue.getClass());
            builder = new IConfigPropertyBuilder<PTYPE>(){
                @Override
                public PTYPE build(String name, String... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValue);
                }

                @Override
                public PTYPE build(String name, Map<String, String> paramsNameMap) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValue);
                }
            };
        }
        catch(NoSuchMethodException e){
            throw new ConfigPropertyTemplateConstructorNotFoundException("Cannot find defaultValue constructor for class <"+clazz.getName()+">",e);
        }
    }

    public ConfigPropertyWithTemplateName(Class<PTYPE> clazz, String nameTemplate, final IConfigProperty<T> defaultValue){
        compileTemplate(nameTemplate);
        try {
            final Constructor<PTYPE> constructor = clazz.getConstructor(String.class, defaultValue.getClass());
            builder = new IConfigPropertyBuilder<PTYPE>(){
                @Override
                public PTYPE build(String name, String... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValue);
                }

                @Override
                public PTYPE build(String name, Map<String, String> paramsNameMap) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValue);
                }
            };
        }
        catch(NoSuchMethodException e){
            throw new ConfigPropertyTemplateConstructorNotFoundException("Cannot find Reference defaultValue constructor for class <"+clazz.getName()+">",e);
        }
    }

    public ConfigPropertyWithTemplateName(Class<PTYPE> clazz, String nameTemplate, final T defaultValue, final ConfigPropertyChangedCallback<T> callback){
        compileTemplate(nameTemplate);
        try {
            final Constructor<PTYPE> constructor = clazz.getConstructor(String.class, defaultValue.getClass(),callback.getClass());
            builder = new IConfigPropertyBuilder<PTYPE>(){
                @Override
                public PTYPE build(String name, String... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValue,callback);
                }

                @Override
                public PTYPE build(String name, Map<String, String> paramsNameMap) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValue,callback);
                }
            };
        }
        catch(NoSuchMethodException e){
            throw new ConfigPropertyTemplateConstructorNotFoundException("Cannot find defaultValue with callback constructor for class <"+clazz.getName()+">",e);
        }
    }

    public ConfigPropertyWithTemplateName(Class<PTYPE> clazz, String nameTemplate, final IConfigProperty<T> defaultValue, final ConfigPropertyChangedCallback<T> callback){
        compileTemplate(nameTemplate);
        try {
            final Constructor<PTYPE> constructor = clazz.getConstructor(String.class, defaultValue.getClass(),callback.getClass());
            builder = new IConfigPropertyBuilder<PTYPE>(){
                @Override
                public PTYPE build(String name, String... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValue,callback);
                }

                @Override
                public PTYPE build(String name, Map<String, String> paramsNameMap) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValue,callback);
                }
            };
        }
        catch(NoSuchMethodException e){
            throw new ConfigPropertyTemplateConstructorNotFoundException("Cannot find Reference defaultValue with callback constructor for class <"+clazz.getName()+">",e);
        }
    }

    public ConfigPropertyWithTemplateName(Class<PTYPE> clazz,String nameTemplate, final ConfigPropertyWithTemplateName<T,PTYPE> defaultValueRefTemplate){
        compileTemplate(nameTemplate);
        try {
            final Constructor<PTYPE> constructor = clazz.getConstructor(String.class, IConfigProperty.class);
            builder = new IConfigPropertyBuilder<PTYPE>(){
                @Override
                public PTYPE build(String name, String... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValueRefTemplate.getProperty(params));
                }

                @Override
                public PTYPE build(String name, Map<String, String> paramsNameMap) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValueRefTemplate.getProperty(paramsNameMap));
                }
            };
        }
        catch(NoSuchMethodException e){
            throw new ConfigPropertyTemplateConstructorNotFoundException("Cannot find Reference defaultValue with callback constructor for class <"+clazz.getName()+">",e);
        }
    }


    public ConfigPropertyWithTemplateName(Class<PTYPE> clazz, String nameTemplate, final ConfigPropertyWithTemplateName<T,PTYPE> defaultValueRefTemplate, final ConfigPropertyChangedCallback<T> callback){
        compileTemplate(nameTemplate);
        try {
            final Constructor<PTYPE> constructor = clazz.getConstructor(String.class, IConfigProperty.class,callback.getClass());
            builder = new IConfigPropertyBuilder<PTYPE>(){
                @Override
                public PTYPE build(String name, String... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValueRefTemplate.getProperty(params), callback);
                }

                @Override
                public PTYPE build(String name, Map<String, String> paramsNameMap) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                    return constructor.newInstance(name, defaultValueRefTemplate.getProperty(paramsNameMap), callback);
                }
            };
        }
        catch(NoSuchMethodException e){
            throw new ConfigPropertyTemplateConstructorNotFoundException("Cannot find Reference defaultValue with callback constructor for class <"+clazz.getName()+">",e);
        }
    }


    private String buildName(String ...values){
        return String.format(template,values);
    }

    private String buildName(Map<String,String> values){
        StringBuilder sb = new StringBuilder();
        for(int paramPos=0;paramPos<templateParts.length;++paramPos){
            sb.append((paramPos%2==0)?templateParts[paramPos]:values.get(templateParts[paramPos]));
        }
        return sb.toString();
    }

    public PTYPE getProperty(String ...values){
        try{
            return builder.build(buildName(values),values);
        }
        catch (IllegalAccessException|InvocationTargetException|InstantiationException e){
            throw new ConfigPropertyTemplateBuildException(e);
        }
    }

    public PTYPE getProperty(Map<String,String> values){
        try{
            return builder.build(buildName(values),values);
        }
        catch (IllegalAccessException|InvocationTargetException|InstantiationException e){
            throw new ConfigPropertyTemplateBuildException(e);
        }
    }

    public interface IConfigPropertyBuilder<PTYPE extends IConfigProperty>{
        PTYPE build(String name,String ...params) throws IllegalAccessException,InvocationTargetException,InstantiationException;
        PTYPE build(String name,Map<String,String> paramsNameMap) throws IllegalAccessException,InvocationTargetException,InstantiationException;
    }

}

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


import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by Christophe Jeunesse on 06/03/2015.
 */
public class AnnotationProcessorVelocityEngine {
    public static final String VELOCITY_VELOCITY_PROPERTIES = "com/dreameddeath/core/velocity/velocity.properties";
    private static VelocityEngine VELOCITY_ENGINE=null;
    private static Map<String,Template> TEMPLATE_MAP=new HashMap<>();


    synchronized public static VelocityEngine getEngine(){
        if(VELOCITY_ENGINE==null) {
            Properties props = new Properties();
            URL url=null;
            for(String name : Arrays.asList(VELOCITY_VELOCITY_PROPERTIES,"/"+VELOCITY_VELOCITY_PROPERTIES)){
                url = Thread.currentThread().getContextClassLoader().getResource(name);
                if(url==null){
                    url = AnnotationProcessorVelocityEngine.class.getClassLoader().getResource(name);
                }
                if(url==null){
                    url = AnnotationProcessorVelocityEngine.class.getResource(name);
                }
                if(url!=null){
                    break;
                }
            }
            if(url==null){
                throw new RuntimeException("Cannot find "+VELOCITY_VELOCITY_PROPERTIES);
            }
            try {
                props.load(url.openStream());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            VELOCITY_ENGINE = new VelocityEngine(props);
            VELOCITY_ENGINE.init();
        }
        return VELOCITY_ENGINE;
    }

    synchronized public static Template getTemplate(String path){
        if(!TEMPLATE_MAP.containsKey(path)){
            TEMPLATE_MAP.put(path, getEngine().getTemplate(path));
        }
        return TEMPLATE_MAP.get(path);
    }

    public static VelocityContext newContext(VelocityLogger logger){
        VelocityContext context = new VelocityContext();

        context.put("esc", new StdEscape());
        context.put("log",logger.logger);
        context.put("message",logger);

        return context;
    }


    public static VelocityContext newContext(VelocityLogger logger,GeneratorInfo generatorInfo){
        VelocityContext context = new VelocityContext();

        context.put("esc", new StdEscape());
        context.put("log",logger.logger);
        context.put("message",logger);
        context.put("generator",generatorInfo);
        return context;
    }
    public static VelocityContext newContext(Logger log,Messager messager){
        return newContext(new VelocityLogger(log,messager));
    }

    public static VelocityContext newContext(Logger log,Messager messager,AbstractProcessor generator,String comment){
        return newContext(new VelocityLogger(log,messager),new GeneratorInfo(generator.getClass(),comment));
    }


    public static void createSource(ProcessingEnvironment env,VelocityContext context,String templateName,String className)throws IOException{
        createSource(env, context, templateName, className,null);
    }

    public static void createSource(ProcessingEnvironment env,VelocityContext context,String templateName,String className,Element... originatingElements)throws IOException{
        JavaFileObject jfo = env.getFiler().createSourceFile(className,originatingElements);
        Writer writer = jfo.openWriter();
        getTemplate(templateName).merge(context, writer);
        writer.close();
        VelocityLogger logger=((VelocityLogger)context.get("message"));
        if(logger!=null) {
            logger.note("Generating source file " + jfo.getName());
        }
    }

    public static void createResourceFile(ProcessingEnvironment env,VelocityContext context,String templateName,String filename)throws IOException{
        createResourceFile(env,context,templateName,filename,null);
    }

    public static void createResourceFile(ProcessingEnvironment env,VelocityContext context,String templateName,String filename,Element... originatingElements)throws IOException{
        FileObject jfo = env.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                "",//Empty Package
                filename);
        Writer writer = jfo.openWriter();
        getTemplate(templateName).merge(context, writer);
        writer.close();
        VelocityLogger logger=((VelocityLogger)context.get("message"));
        if(logger!=null) {
            logger.note("Generating resource file " + jfo.getName());
        }
    }

    public static class VelocityLogger{
        private Logger logger;
        private Messager messager;

        public VelocityLogger(Logger logger, Messager messager){
            this.logger=logger;
            this.messager = messager;
        }

        public void note(String message){
            messager.printMessage(Diagnostic.Kind.NOTE, message);
        }

        public void error(String message){
            messager.printMessage(Diagnostic.Kind.ERROR, message);
        }

    }

    public static class StdEscape{
        public String java(String input){
            return StringEscapeUtils.escapeJava(input);
        }
        public String html(String input){
            return StringEscapeUtils.escapeHtml4(input);
        }
        public String xml(String input){
            return StringEscapeUtils.escapeXml11(input);
        }
        public String json(String input){
            return StringEscapeUtils.escapeJson(input);
        }
        public String javascript(String input){
            return StringEscapeUtils.escapeEcmaScript(input);
        }
    }

    public static class GeneratorInfo{
        private String generatorName;
        private DateTime dateTime = DateTime.now();
        private String comment;

        public GeneratorInfo(Class processorClass,String comment){
            generatorName = processorClass.getName();
            this.comment = comment;
        }

        public DateTime getDate() {
            return dateTime;
        }

        public String getName() {
            return generatorName;
        }

        public String getComment() {
            return comment;
        }
    }
}

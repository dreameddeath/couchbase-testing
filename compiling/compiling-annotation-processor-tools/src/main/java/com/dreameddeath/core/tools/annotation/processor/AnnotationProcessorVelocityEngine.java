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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by ceaj8230 on 06/03/2015.
 */
public class AnnotationProcessorVelocityEngine {
    private static VelocityEngine VELOCITY_ENGINE=null;
    private static Map<String,Template> TEMPLATE_MAP=new HashMap<>();


    synchronized public static VelocityEngine getEngine(){
        if(VELOCITY_ENGINE==null) {
            Properties props = new Properties();
            URL url = AnnotationProcessorVelocityEngine.class.getClassLoader().getResource("com/dreameddeath/core/velocity/velocity.properties");
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
        context.put("log",logger._logger);
        context.put("message",logger);

        return context;
    }


    public static VelocityContext newContext(VelocityLogger logger,GeneratorInfo generatorInfo){
        VelocityContext context = new VelocityContext();

        context.put("esc", new StdEscape());
        context.put("log",logger._logger);
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
        ((VelocityLogger)context.get("message")).note("Generating source file " + jfo.getName());
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
        ((VelocityLogger)context.get("message")).note("Generating resource file " + jfo.getName());
    }

    public static class VelocityLogger{
        private Logger _logger;
        private Messager _messager;

        public VelocityLogger(Logger logger, Messager messager){
            _logger=logger;
            _messager = messager;
        }

        public void note(String message){
            _messager.printMessage(Diagnostic.Kind.NOTE, message);
        }

        public void error(String message){
            _messager.printMessage(Diagnostic.Kind.ERROR, message);
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
        private String _generatorName;
        private DateTime _dateTime = DateTime.now();
        private String _comment;

        public GeneratorInfo(Class processorClass,String comment){
            _generatorName = processorClass.getName();
            _comment = comment;
        }

        public DateTime getDate() {
            return _dateTime;
        }

        public String getName() {
            return _generatorName;
        }

        public String getComment() {
            return _comment;
        }
    }
}

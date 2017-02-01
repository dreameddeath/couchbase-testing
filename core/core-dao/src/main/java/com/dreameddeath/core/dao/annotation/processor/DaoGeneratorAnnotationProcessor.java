/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.dao.annotation.processor;


import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationElementType;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessorVelocityEngine;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.annotation.dao.View;
import com.dreameddeath.core.dao.annotation.processor.model.*;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 29/12/2014.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.dao.annotation.dao.DaoEntity"}
)
public class DaoGeneratorAnnotationProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DaoGeneratorAnnotationProcessor.class);
    private static final String TEMPLATE_DAO_FILENAME = "core/templates/stdDaoTemplate.vm";



    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        List<IDaoGeneratorPlugin> globalPlugins = new ArrayList<>();

        List<URL> classPath = Arrays.asList(((URLClassLoader)DaoEntity.class.getClassLoader()).getURLs());
        messager.printMessage(Diagnostic.Kind.NOTE, "Loading global plugins with classPath :"+classPath);
        {
            final ServiceLoader<IDaoGeneratorPlugin> pluginServiceLoader = ServiceLoader.load(IDaoGeneratorPlugin.class,DaoEntity.class.getClassLoader());
            Iterator<IDaoGeneratorPlugin> pluginIterator = pluginServiceLoader.iterator();
            while (pluginIterator.hasNext()) {
                try {
                    IDaoGeneratorPlugin generatorPlugin = pluginIterator.next();
                    messager.printMessage(Diagnostic.Kind.NOTE, DaoGeneratorAnnotationProcessor.class.getSimpleName()+ ": Loading global plugin " + generatorPlugin.getClass().getName());
                    globalPlugins.add(generatorPlugin);
                }
                catch(Throwable e){
                    messager.printMessage(Diagnostic.Kind.WARNING,DaoGeneratorAnnotationProcessor.class.getSimpleName()+ ": cannot load a plugin with message "+e.getMessage());
                }
            }
        }

        AnnotationElementType.CURRENT_ELEMENT_UTILS.set(processingEnv.getElementUtils());
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(DaoEntity.class)) {
                if (!CouchbaseDocumentReflection.isReflexible(element)) continue;
                manageDaoLayerGeneration(globalPlugins,processingEnv.getFiler(),messager,element);
            }

        }
        catch(Throwable e){
            LOG.error("Error during processing",e);
            StringBuffer buf = new StringBuffer();
            for(StackTraceElement elt:e.getStackTrace()){
                buf.append(elt.toString());
                buf.append("\n");
            }
            messager.printMessage(Diagnostic.Kind.ERROR,"Error during processing "+e.getMessage()+"\n"+buf.toString());
            throw new RuntimeException("Error during annotation processor",e);
        }
        return true;
    }



    public void manageDaoLayerGeneration(List<IDaoGeneratorPlugin> globalPlugins,Filer filer, Messager messager, Element element) throws IOException{
        CouchbaseDocumentReflection docReflection = CouchbaseDocumentReflection.getReflectionFromTypeElement((TypeElement) element);
        LOG.debug("Starting to process {}", docReflection.getSimpleName());
        LOG.debug("With DaoEntity to process {}", Arrays.asList(docReflection.getClassInfo().getAnnotationByType(DaoEntity.class)));


        VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG,messager,this,"Generated from "+docReflection.getName());

        GlobalDef globalDef = new GlobalDef();
        globalDef.setEntity(new EntityDef(docReflection));

        globalDef.setDaoDef(new DaoDef(docReflection));
        globalDef.setDbPathDef(new DbPathDef(docReflection));

        List<Counter> countersAnnotation = Arrays.asList(docReflection.getClassInfo().getAnnotationByType(Counter.class));
        for (Counter counterAnnot : countersAnnotation) {
            CounterDef newCounter = new CounterDef(docReflection, counterAnnot, globalDef.getDbPathDef());
            globalDef.getCounterDefList().add(newCounter);
        }

        List<View> viewsAnnotation = Arrays.asList(docReflection.getClassInfo().getAnnotationByType(View.class));
        for(View viewAnnot : viewsAnnotation){
            ViewDef newView = new ViewDef(filer,globalDef.getEntity(),docReflection,viewAnnot);
            globalDef.getViewDefList().add(newView);
        }

        for(IDaoGeneratorPlugin globalPlugin:globalPlugins){
            globalPlugin.manage(globalDef,messager,element);
        }

        DaoEntity daoEntityAnnot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
        if(daoEntityAnnot!=null) {
            AbstractClassInfo[] classInfos=AbstractClassInfo.getClassInfoFromAnnot(daoEntityAnnot,DaoEntity::plugins);
            if(classInfos!=null) {
                for (AbstractClassInfo pluginClass : classInfos) {
                    try {
                        IDaoGeneratorPlugin plugin = (IDaoGeneratorPlugin) pluginClass.getCurrentClass().newInstance();
                        plugin.manage(globalDef, messager, element);
                    } catch (Throwable e) {
                        throw new RuntimeException("Error during management of plugin " + pluginClass.getName(), e);
                    }
                }
            }
        }
        globalDef.fillContext(context);
        AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_DAO_FILENAME, globalDef.getDaoDef().getName(), element);
    }

}

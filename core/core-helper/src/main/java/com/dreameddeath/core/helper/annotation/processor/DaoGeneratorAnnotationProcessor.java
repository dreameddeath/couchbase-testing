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

package com.dreameddeath.core.helper.annotation.processor;


import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationElementType;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessorVelocityEngine;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.helper.annotation.dao.Counter;
import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.helper.annotation.dao.View;
import com.dreameddeath.core.helper.annotation.processor.model.*;
import com.dreameddeath.core.helper.annotation.service.RestDao;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 29/12/2014.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.helper.annotation.dao.DaoEntity",
        "com.dreameddeath.core.helper.annotation.service.RestDao"}
)
public class DaoGeneratorAnnotationProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DaoGeneratorAnnotationProcessor.class);
    private static final String TEMPLATE_DAO_FILENAME = "core/templates/stdDaoTemplate.vm";
    private static final String TEMPLATE_READ_REST_FILENAME = "core/templates/stdReadRestServiceTemplate.vm";
    private static final String TEMPLATE_WRITE_REST_FILENAME = "core/templates/stdWriteRestServiceTemplate.vm";


    /**
     *  Provide a hash map for just generated classes
     */
    private Map<ClassInfo,DaoDef> _daoClassMap=new HashMap<>();



    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        AnnotationElementType.CURRENT_ELEMENT_UTILS.set(processingEnv.getElementUtils());
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(DaoEntity.class)) {
                if (!CouchbaseDocumentReflection.isReflexible(element)) continue;
                manageDaoLayerGeneration(messager,element);
            }

            for (Element element : roundEnv.getElementsAnnotatedWith(RestDao.class)) {
                manageRestLayerGeneration(messager, element);
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


    public void manageRestLayerGeneration(Messager messager,Element element) throws IOException{
        //CouchbaseDocumentReflection docReflection = CouchbaseDocumentReflection.getReflectionFromTypeElement((TypeElement) element);
        ClassInfo daoClassInfo = (ClassInfo)AbstractClassInfo.getClassInfo((TypeElement)element);
        VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager, this, "Generated from " + daoClassInfo.getImportName());

        DaoRestServiceDef restDef= new DaoRestServiceDef(daoClassInfo,_daoClassMap);
        context.put("service",restDef);

        AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_READ_REST_FILENAME, restDef.getReadFullName(), element);
        AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_WRITE_REST_FILENAME, restDef.getWriteFullName(), element);
    }

    public void manageDaoLayerGeneration(Messager messager,Element element) throws IOException{
        CouchbaseDocumentReflection docReflection = CouchbaseDocumentReflection.getReflectionFromTypeElement((TypeElement) element);
        LOG.debug("Starting to process {}", docReflection.getSimpleName());
        LOG.debug("With DaoEntity to process {}", docReflection.getClassInfo().getAnnotationByType(DaoEntity.class).toString());


        VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG,messager,this,"Generated from "+docReflection.getName());

        EntityDef entity = new EntityDef(docReflection);
        context.put("entity", entity);

        DaoDef daoDef = new DaoDef(docReflection);
        context.put("daoDef", daoDef);
        _daoClassMap.put(docReflection.getClassInfo(),daoDef);
        DbPathDef dbPathDef = new DbPathDef(docReflection);
        context.put("dbPath", dbPathDef);

        List<CounterDef> counterDefList = new ArrayList<>();
        List<Counter> countersAnnotation = Arrays.asList(docReflection.getClassInfo().getAnnotationByType(Counter.class));

        for (Counter counterAnnot : countersAnnotation) {
            CounterDef newCounter = new CounterDef(docReflection, counterAnnot, dbPathDef);
            counterDefList.add(newCounter);
            messager.printMessage(Diagnostic.Kind.NOTE, "Generating counter " + newCounter.toString());

            if (newCounter.isKeyGen()) {
                context.put("keyCounter", newCounter);
            }
        }

        context.put("counters", counterDefList);

        List<ViewDef> viewDefList = new ArrayList<>();
        context.put("views", viewDefList);
        List<View> viewsAnnotation = Arrays.asList(docReflection.getClassInfo().getAnnotationByType(View.class));
        for(View viewAnnot : viewsAnnotation){
            ViewDef newView = new ViewDef(entity,docReflection,viewAnnot);
            viewDefList.add(newView);
            messager.printMessage(Diagnostic.Kind.NOTE,"Generating view "+ newView.toString());
        }

        AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_DAO_FILENAME, daoDef.getName(), element);
    }

}

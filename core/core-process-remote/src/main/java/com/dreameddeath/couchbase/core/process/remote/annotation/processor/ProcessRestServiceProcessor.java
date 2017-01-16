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

package com.dreameddeath.couchbase.core.process.remote.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessorVelocityEngine;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;
import com.dreameddeath.couchbase.core.process.remote.annotation.processor.model.EnumModel;
import com.dreameddeath.couchbase.core.process.remote.annotation.processor.model.RemoteServiceInfo;
import com.dreameddeath.couchbase.core.process.remote.annotation.processor.model.RestModel;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose"}
)
public class ProcessRestServiceProcessor extends AbstractAnnotationProcessor{
    private static final Logger LOG = LoggerFactory.getLogger(ProcessRestServiceProcessor.class);
    private static final String TEMPLATE_SERVICE_REST_FILENAME = "core/templates/rest.process.server.vm";
    private static final String TEMPLATE_MODEL_REST_FILENAME = "core/templates/rest.process.model.vm";
    private static final String TEMPLATE_ENUM_REST_FILENAME = "core/templates/rest.process.enum.vm";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        EntityDefinitionManager entityManager = new EntityDefinitionManager();
        Set<EntityDef> modelIds = new HashSet<>();
        modelIds.addAll(entityManager.getEntities());
        for(Element localEntityElement:roundEnv.getElementsAnnotatedWith(DocumentEntity.class)){
            if(CouchbaseDocumentStructureReflection.isReflexible(localEntityElement)) {
                CouchbaseDocumentStructureReflection documentStructureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)AbstractClassInfo.getClassInfo((TypeElement)localEntityElement));
                modelIds.add(EntityDef.build(documentStructureReflection));
            }
        }

        for(Element classElem : roundEnv.getElementsAnnotatedWith(RestExpose.class)) {
            try{
                ClassInfo jobClassInfo = (ClassInfo)AbstractClassInfo.getClassInfo((TypeElement) classElem);
                RemoteServiceInfo serviceInfo = new RemoteServiceInfo(jobClassInfo,modelIds);
                {
                    VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager, this, "Generated from " + jobClassInfo.getImportName());
                    context.put("service", serviceInfo);
                    AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_SERVICE_REST_FILENAME, serviceInfo.getFullName(), classElem);
                }
                for(Map.Entry<String,RestModel> modelEntry:serviceInfo.getModels().entrySet()){
                    if(modelEntry.getValue().isUnwrapped){continue;}
                    VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager, this, "Generated from " + jobClassInfo.getImportName() + " for "+modelEntry.getKey());
                    context.put("model", modelEntry.getValue());
                    AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_MODEL_REST_FILENAME, modelEntry.getValue().getImportName(), classElem);
                }

                for(Map.Entry<String,EnumModel> modelEntry:serviceInfo.getJob().getEnums().entrySet()){
                    VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager, this, "Generated from " + jobClassInfo.getImportName() + " for "+modelEntry.getKey());
                    context.put("model", modelEntry.getValue());
                    AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_ENUM_REST_FILENAME, modelEntry.getValue().getImportName(), classElem);
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
        }
        return true;
    }
}

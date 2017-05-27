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

package com.dreameddeath.core.model.dto.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessorVelocityEngine;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.dto.annotation.DtoGenerate;
import com.dreameddeath.core.model.dto.annotation.processor.model.ConverterServiceInfo;
import com.dreameddeath.core.model.dto.annotation.processor.model.DtoModel;
import com.dreameddeath.core.model.dto.annotation.processor.model.EnumModel;
import com.dreameddeath.core.model.dto.converter.DtoConverterManager;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.squareup.javapoet.JavaFile;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Map;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.model.annotation.DocumentEntity"}
)
public class DtoGeneratorProcessor extends AbstractAnnotationProcessor{
    private static final Logger LOG = LoggerFactory.getLogger(DtoGeneratorProcessor.class);
    private static final String TEMPLATE_CONVERTER_DTO_FILENAME = "core/templates/dto.converter.vm";
    private static final String TEMPLATE_MODEL_DTO_FILENAME = "core/templates/dto.model.vm";
    private static final String TEMPLATE_ENUM_DTO_FILENAME = "core/templates/dto.enum.vm";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        EntityDefinitionManager entityManager = new EntityDefinitionManager();
        DtoConverterManager converterManager = new DtoConverterManager();
        ConverterGeneratorContext converterGeneratorContext = new ConverterGeneratorContext();
        converterGeneratorContext.addAllEntities(entityManager.getEntities());
        converterGeneratorContext.addAllConvertersDef(converterManager.getCachedConvertersDef());

        for(Element localEntityElement:roundEnv.getElementsAnnotatedWith(DocumentEntity.class)){
            CouchbaseDocumentStructureReflection documentStructureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)AbstractClassInfo.getClassInfo((TypeElement)localEntityElement));
            converterGeneratorContext.addEntityDef(EntityDef.build(documentStructureReflection));
        }

        for(Element classElem : roundEnv.getElementsAnnotatedWith(DocumentEntity.class)) {
            try{
                ClassInfo entityClassInfo = (ClassInfo)AbstractClassInfo.getClassInfo((TypeElement) classElem);
                DtoGenerate[] dtoGenerateAnnotations = entityClassInfo.getAnnotationByType(DtoGenerate.class);
                if(dtoGenerateAnnotations!=null && dtoGenerateAnnotations.length > 0) {
                    for(DtoGenerate dtoAnnot : dtoGenerateAnnotations) {
                        for(String version:dtoAnnot.buildForVersions()) {
                            ConverterServiceInfo serviceInfo = new ConverterServiceInfo(entityClassInfo, converterGeneratorContext, dtoAnnot,version);
                            JavaFile javaFile = serviceInfo.getJavaFile();
                            javaFile.writeTo(processingEnv.getFiler());
                        }
                    }
                }
                else{
                    ConverterServiceInfo serviceInfo = new ConverterServiceInfo(entityClassInfo, converterGeneratorContext,null, "1.0");
                    JavaFile javaFile = serviceInfo.getJavaFile();
                    javaFile.writeTo(processingEnv.getFiler());
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

        try {
            for (Map.Entry<String, DtoModel> modelEntry : converterGeneratorContext.getDtoModels().entrySet()) {
                if (modelEntry.getValue().isUnwrapped) {
                    continue;
                }
                VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager, this, "Generated for " + modelEntry.getKey());
                context.put("model", modelEntry.getValue());
                AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_MODEL_DTO_FILENAME, modelEntry.getValue().getImportName());
            }

            for (Map.Entry<String, EnumModel> modelEntry : converterGeneratorContext.getEnums().entrySet()) {
                VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager, this, "Generated for " + modelEntry.getKey());
                context.put("model", modelEntry.getValue());
                AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_ENUM_DTO_FILENAME, modelEntry.getValue().getImportName());
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
}

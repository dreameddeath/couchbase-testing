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


import com.dreameddeath.core.service.annotation.ExposeService;
import com.dreameddeath.core.tools.annotation.processor.AnnotationElementType;
import com.dreameddeath.core.tools.annotation.processor.AnnotationProcessorVelocityEngine;
import com.dreameddeath.core.tools.annotation.processor.reflection.AbstractClassInfo;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 06/03/2015.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.service.annotation.ExposeService"}
)
public class ServiceExposeAnnotationProcessor extends AbstractProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceExposeAnnotationProcessor.class);
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        AnnotationElementType.CURRENT_ELEMENT_UTILS.set(processingEnv.getElementUtils());
        for(Element classElem : roundEnv.getElementsAnnotatedWith(ExposeService.class)){
            AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement) classElem);
            if(classInfo.getEnclosingClass()!=null){
                throw new RuntimeException("The service <"+classInfo.getFullName()+"> shouldn't be an inner class to allow Rest Services Annotation through annotation @ExposeService");
            }
            VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager,this,String.format("Generator of Service Rest from %s",classInfo.getFullName()));

            ServiceExpositionDef serviceDef = new ServiceExpositionDef(classInfo);
            context.put("service",serviceDef);
            try {
                AnnotationProcessorVelocityEngine.createSource(processingEnv,context,"com/dreameddeath/core/service/exposedServiceClient.vm",serviceDef.getClientClassName(),classElem);
            } catch (IOException e) {
                throw new RuntimeException("Cannot generate element",e);
            }

            try {
                AnnotationProcessorVelocityEngine.createSource(processingEnv,context,"com/dreameddeath/core/service/exposedServiceServer.vm",serviceDef.getServerClassName(),classElem);
            } catch (IOException e) {
                throw new RuntimeException("Cannot generate element",e);
            }

        }
        return true;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

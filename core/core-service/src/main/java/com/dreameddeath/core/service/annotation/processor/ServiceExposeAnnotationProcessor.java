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

package com.dreameddeath.core.service.annotation.processor;


import com.dreameddeath.core.service.annotation.ExposeService;
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
import java.util.Set;

/**
 * Created by ceaj8230 on 06/03/2015.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.annotation.ExposeService"}
)
public class ServiceExposeAnnotationProcessor extends AbstractProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceExposeAnnotationProcessor.class);
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        for(Element classElem : roundEnv.getElementsAnnotatedWith(ExposeService.class)){
            AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement) classElem);
            VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager);

            ServiceExpositionDef serviceDef = new ServiceExpositionDef(classInfo);

        }
        return true;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

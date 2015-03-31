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

        }
        return true;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

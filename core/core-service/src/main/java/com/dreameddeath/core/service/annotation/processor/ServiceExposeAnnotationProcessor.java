package com.dreameddeath.core.service.annotation.processor;


import com.dreameddeath.core.service.annotation.ExposeService;

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
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        for(Element classElem : roundEnv.getElementsAnnotatedWith(ExposeService.class)){


        }
        return true;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

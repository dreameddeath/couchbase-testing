package com.dreameddeath.core.model.dto.annotation.processor.converter;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.DtoModelMappingInfo;
import com.dreameddeath.core.model.dto.converter.DtoConverterManager;
import com.dreameddeath.core.model.dto.converter.model.DtoConverterDef;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;


/**
 * Created by CEAJ8230 on 05/06/2017.
 */

@SupportedAnnotationTypes(
        {"com.dreameddeath.core.model.dto.annotation.DtoModelMappingInfo"}
)
public class ConverterGeneratorProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ConverterGeneratorProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        DtoConverterManager converterManager = new DtoConverterManager();
        StandardConverterGenerator converterGenerator = new StandardConverterGenerator();

        try {
            for (DtoConverterDef def : converterManager.getConverterDefs()) {
                AbstractClassInfo converterClassInfo = ClassInfo.getClassInfo(def.getConverterClass());

                if (StringUtils.isNotEmpty(def.getInputClass())) {
                    ClassInfo inputClassInfo = (ClassInfo)ClassInfo.getClassInfo(def.getInputClass());
                    converterGenerator.addExistingConverter(inputClassInfo, converterClassInfo.getClassName());
                }
                if (StringUtils.isNotEmpty(def.getOutputClass())) {
                    ClassInfo outputClassInfo = (ClassInfo)ClassInfo.getClassInfo(def.getOutputClass());
                    converterGenerator.addExistingConverter(outputClassInfo, ClassName.bestGuess(def.getConverterClass()));
                }
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

        for (Element classElem : roundEnv.getElementsAnnotatedWith(DtoModelMappingInfo.class)) {
            try{
                ClassInfo dtoModelClassInfo = (ClassInfo) AbstractClassInfo.getClassInfo((TypeElement) classElem);
                if(!dtoModelClassInfo.isEnum()) {
                    converterGenerator.buildConverter(dtoModelClassInfo);
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

        for(JavaFile javaFile:converterGenerator.getFiles()){
            try {
                javaFile.writeTo(processingEnv.getFiler());
                messager.printMessage(Diagnostic.Kind.NOTE, "Generating converter from @dtogenerate " + javaFile.typeSpec.name);
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

        return false;
    }
}
package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.dto.model.manager.DtoModelManager;
import com.squareup.javapoet.JavaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

/**
 * Created by CEAJ8230 on 03/06/2017.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.model.annotation.DocumentEntity"}
)
public class StandardCouchbaseDocumentDtoModelGeneratorProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(StandardCouchbaseDocumentDtoModelGeneratorProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        DtoModelManager manager = new DtoModelManager();
        AbstractDtoModelGenerator processor = new StandardCouchbaseDocumentDtoModelGenerator(manager);

        for (Element classElem : roundEnv.getElementsAnnotatedWith(DocumentEntity.class)) {
            try {
                ClassInfo entityClassInfo = (ClassInfo) AbstractClassInfo.getClassInfo((TypeElement) classElem);
                processor.generateIfNeeded(entityClassInfo);
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
        for(JavaFile javaFile:processor.getJavaFiles()){
            try {
                javaFile.writeTo(processingEnv.getFiler());
                messager.printMessage(Diagnostic.Kind.NOTE, "Generating converter " + javaFile.typeSpec.name);
            }
            catch(IOException e){
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

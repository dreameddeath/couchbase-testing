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

package com.dreameddeath.core.couchbase.annotation.processor;


import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessFileUtils;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.couchbase.utils.CouchbaseUtils;
import com.dreameddeath.core.model.annotation.DocumentEntity;
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
 * Created by Christophe Jeunesse on 09/08/2015.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass"}
)
public class CouchbaseAnnotationProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseAnnotationProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(BucketDocumentForClass.class)) {
                try {
                    AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement) element);
                    BucketDocumentForClass annot = classInfo.getAnnotation(BucketDocumentForClass.class);
                    AbstractClassInfo targetClassInfo = AbstractClassInfo.getClassInfoFromAnnot(annot, BucketDocumentForClass::value);
                    if(targetClassInfo.getAnnotation(DocumentEntity.class)==null){
                        throw new RuntimeException("The class <"+targetClassInfo.getFullName()+"> must have an @DocumentDef annotation as it is the target of a BucketDocumentForClass annotation");
                    }
                    String fileName= CouchbaseUtils.getTargetBucketDocumentRegisteringFilename(annot);
                    AnnotationProcessFileUtils.ResourceFile file = AnnotationProcessFileUtils.createResourceFile(processingEnv, fileName, element);
                    file.getWriter().write(classInfo.getFullName());
                    file.close();
                }
                catch(IOException e){
                    messager.printMessage(Diagnostic.Kind.ERROR,"Cannot write with error"+e.getMessage());
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
        return true;
    }
}

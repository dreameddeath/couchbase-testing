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

package com.dreameddeath.core.model.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessFileUtils;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.entity.EntityModelId;
import com.dreameddeath.core.model.upgrade.Utils;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 26/11/2014.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.model.annotation.DocumentDef"}
)
public class DocumentDefAnnotationProcessor extends AbstractAnnotationProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        for(Element classElem:roundEnv.getElementsAnnotatedWith(DocumentDef.class)){
            DocumentDef annot =classElem.getAnnotation(DocumentDef.class);
            try {
                String fileName= Utils.getDocumentEntityFilename(EntityModelId.build(annot, classElem));
                AnnotationProcessFileUtils.ResourceFile file = AnnotationProcessFileUtils.createResourceFile(processingEnv, fileName, classElem);
                AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement)classElem);
                file.getWriter().write(classInfo.getFullName());
                file.close();
            }
            catch(IOException e){
                messager.printMessage(Diagnostic.Kind.ERROR,"Cannot write with error"+e.getMessage());
            }
        }
        return true;
    }

}

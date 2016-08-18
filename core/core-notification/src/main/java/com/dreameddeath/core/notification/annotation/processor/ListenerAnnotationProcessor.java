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

package com.dreameddeath.core.notification.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessFileUtils;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 16/08/2016.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.notification.annotation.ListenerForTypes",
        "com.dreameddeath.core.notification.annotation.Listener",
        }
)

public class ListenerAnnotationProcessor extends AbstractAnnotationProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        ListenerInfoManager listenerTypeManager= new ListenerInfoManager();

        for(Element classElem:roundEnv.getElementsAnnotatedWith(Listener.class)){
            ClassInfo classInfo = (ClassInfo)ClassInfo.getClassInfo((TypeElement) classElem);
            Listener annot =classElem.getAnnotation(Listener.class);
            {
                try {
                    String fileName = listenerTypeManager.getFilenameFromClass(classInfo);
                    AnnotationProcessFileUtils.ResourceFile file = AnnotationProcessFileUtils.createResourceFile(processingEnv, fileName, classElem);
                    listenerTypeManager.buildClassDefinitionFile(file.getWriter(),classInfo,annot);
                    file.close();
                }
                catch(IOException e){
                    messager.printMessage(Diagnostic.Kind.ERROR, "Cannot write with error" + e.getMessage());
                    throw new RuntimeException(e);
                }

            }

            for(String typeStr:annot.forTypes()) {
                try {
                    String typeInfoFilename = listenerTypeManager.getFilenameFromType(typeStr);
                    AnnotationProcessFileUtils.ResourceFile file = AnnotationProcessFileUtils.createResourceFile(processingEnv, typeInfoFilename, classElem);
                    listenerTypeManager.buildTypeDefinitionFile(file.getWriter(),classInfo,annot,typeStr);
                    file.close();
                } catch (IOException e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Cannot write with error" + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }
}

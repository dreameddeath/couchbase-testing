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

package com.dreameddeath.core.dao.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessFileUtils;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.factory.DaoUtils;
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
        {"com.dreameddeath.core.dao.annotation.DaoForClass"}
)
public class DaoAnnotationProcessor extends AbstractAnnotationProcessor {
        private static final Logger LOG = LoggerFactory.getLogger(DaoAnnotationProcessor.class);

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            Messager messager = processingEnv.getMessager();
            try {
                for (Element element : roundEnv.getElementsAnnotatedWith(DaoForClass.class)) {
                    try {
                        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement) element);
                        {
                            String fileName = DaoUtils.getTargetDaoPerClassRegisteringFilename(classInfo.getAnnotation(DaoForClass.class));
                            AnnotationProcessFileUtils.ResourceFile file = AnnotationProcessFileUtils.createResourceFile(processingEnv, fileName, element);
                            DaoUtils.writeDaoInfo(file.getWriter(), classInfo.getAnnotation(DaoForClass.class), (ClassInfo) classInfo);
                            file.close();
                        }
                        {
                            String fileName = DaoUtils.getTargetDaoPerModelRegisteringFilename(classInfo.getAnnotation(DaoForClass.class));
                            AnnotationProcessFileUtils.ResourceFile file = AnnotationProcessFileUtils.createResourceFile(processingEnv, fileName, element);
                            DaoUtils.writeDaoInfo(file.getWriter(), classInfo.getAnnotation(DaoForClass.class), (ClassInfo) classInfo);
                            file.close();
                        }
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

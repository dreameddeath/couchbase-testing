/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.config.annotation.processor;


import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessFileUtils;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
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
        {"com.dreameddeath.core.config.annotation.ConfigPropertyPackage"}
)
public class ConfigAnnotationProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigAnnotationProcessor.class);

    private AnnotationProcessFileUtils.ResourceFile resourceFile;
    @Override
    public void init(ProcessingEnvironment env){
        super.init(env);
        try {
            resourceFile = AnnotationProcessFileUtils.createResourceFile(processingEnv, ConfigPropertyFactory.LISTING_CONFIG_FILE, null);
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        try {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ConfigPropertyPackage.class);
            for (Element element : elements) {
                AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement) element);
                resourceFile.getWriter().append(classInfo.getName());
                resourceFile.getWriter().append("\n");
                resourceFile.getWriter().flush();
            }
        }
        catch(Throwable e){
            LOG.error("Error during processing",e);
            StringBuilder buf = new StringBuilder();
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

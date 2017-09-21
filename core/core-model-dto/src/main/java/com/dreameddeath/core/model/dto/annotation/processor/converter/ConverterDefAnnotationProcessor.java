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

package com.dreameddeath.core.model.dto.annotation.processor.converter;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessFileUtils;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.dto.annotation.DtoConverterForEntity;
import com.dreameddeath.core.model.dto.converter.DtoConverterManager;
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
 * Created by christophe jeunesse on 07/03/2017.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.core.model.dto.annotation.DtoConverterForEntity"}
)
public class ConverterDefAnnotationProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(ConverterDefAnnotationProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        DtoConverterManager manager = new DtoConverterManager();

        for (Element converterElement : roundEnv.getElementsAnnotatedWith(DtoConverterForEntity.class)) {
            ClassInfo converterClassInfo = (ClassInfo) AbstractClassInfo.getClassInfo((TypeElement) converterElement);
            String fileName = manager.getConverterDefPath(converterClassInfo);
            try {
                AnnotationProcessFileUtils.ResourceFile file = AnnotationProcessFileUtils.createResourceFile(processingEnv, fileName, converterElement);
                manager.buildConverterDefFile(file.getWriter(), converterClassInfo);
                file.close();
            }
            catch(IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,"Cannot write marker file for" + converterClassInfo.getClassName() + " with error "+e.getMessage());
                throw new RuntimeException("Marker file generation error for "+converterClassInfo.getClassName(),e);
            }
        }
        return false;
    }
}
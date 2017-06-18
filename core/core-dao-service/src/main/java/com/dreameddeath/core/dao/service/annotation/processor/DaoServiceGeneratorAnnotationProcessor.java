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

package com.dreameddeath.core.dao.service.annotation.processor;


import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationElementType;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessorVelocityEngine;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.service.annotation.processor.model.DaoRestServiceDef;
import com.dreameddeath.core.dao.service.annotation.service.RestDao;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 29/12/2014.
 */
@SupportedAnnotationTypes(
                {
                "com.dreameddeath.core.dao.service.annotation.service.RestDao"}
)
public class DaoServiceGeneratorAnnotationProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DaoServiceGeneratorAnnotationProcessor.class);
    private static final String TEMPLATE_READ_REST_FILENAME = "core/templates/stdReadRestServiceTemplate.vm";
    private static final String TEMPLATE_WRITE_REST_FILENAME = "core/templates/stdWriteRestServiceTemplate.vm";


    /**
     *  Provide a hash map for just generated classes
     */
    private Map<ClassInfo,AbstractClassInfo> daoClassMap=new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        AnnotationElementType.CURRENT_ELEMENT_UTILS.set(processingEnv.getElementUtils());
        try {
            //For those not yet generated
            for (Element element : roundEnv.getElementsAnnotatedWith(DaoForClass.class)) {
                ClassInfo daoClassInfo = (ClassInfo)AbstractClassInfo.getClassInfo((TypeElement)element);
                DaoForClass daoForClassAnnot = daoClassInfo.getAnnotation(DaoForClass.class);
                ClassInfo entityClassInfo = (ClassInfo)AbstractClassInfo.getClassInfoFromAnnot(daoForClassAnnot,DaoForClass::value);
                daoClassMap.put(entityClassInfo,daoClassInfo);
            }

            for (Element element : roundEnv.getElementsAnnotatedWith(RestDao.class)) {
                manageRestLayerGeneration(messager, element);
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


    public void manageRestLayerGeneration(Messager messager, Element element) throws IOException{
        ClassInfo daoClassInfo = (ClassInfo)AbstractClassInfo.getClassInfo((TypeElement)element);
        VelocityContext context = AnnotationProcessorVelocityEngine.newContext(LOG, messager, this, "Generated from " + daoClassInfo.getImportName());

        DaoRestServiceDef restDef= new DaoRestServiceDef(daoClassInfo,daoClassMap);
        context.put("service",restDef);

        AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_READ_REST_FILENAME, restDef.getReadFullName(), element);
        AnnotationProcessorVelocityEngine.createSource(processingEnv, context, TEMPLATE_WRITE_REST_FILENAME, restDef.getWriteFullName(), element);
    }

}

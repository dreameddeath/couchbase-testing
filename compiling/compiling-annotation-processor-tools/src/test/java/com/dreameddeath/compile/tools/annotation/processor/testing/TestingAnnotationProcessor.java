/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.compile.tools.annotation.processor.testing;

import com.dreameddeath.compile.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationElementType;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessorVelocityEngine;
import com.dreameddeath.compile.tools.annotation.processor.reflection.*;
import org.apache.velocity.VelocityContext;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Christophe Jeunesse on 06/03/2015.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.compile.tools.annotation.processor.testing.TestingAnnotation"}
)
public class TestingAnnotationProcessor extends AbstractAnnotationProcessor {
    private static final Logger LOG= LoggerFactory.getLogger(TestingAnnotationProcessor.class);

    private int nbProcessedClasses = 0;

    public int getNbProcessedClasses() {
        return nbProcessedClasses;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        Elements elementUtils = processingEnv.getElementUtils();
        AnnotationElementType.CURRENT_ELEMENT_UTILS.set(elementUtils);
        AnnotationProcessorVelocityEngine.VelocityLogger velocityLogger = new AnnotationProcessorVelocityEngine.VelocityLogger(LOG,messager, this);
        for(Element baseElem:roundEnv.getElementsAnnotatedWith(TestingAnnotation.class)){
            nbProcessedClasses++;
            try {
                AnnotatedInfo elemInfo= AnnotationElementType.getInfoOf(baseElem);
                if(baseElem instanceof TypeElement) {
                    AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement)baseElem);
                    if(classInfo.getSimpleName().equals("ExtendsGenericClassWithGenerics")){
                        ClassInfo classWithGenericsInfo = ((ClassInfo)classInfo).getSuperClass();
                        FieldInfo field = classWithGenericsInfo.getFieldByName("value");
                        MethodInfo info = classWithGenericsInfo.getMethod("classWithTreq", field.getType());
                        assertNotNull(info);
                        info = classWithGenericsInfo.getMethod("setStatus",classWithGenericsInfo.getFieldByName("status").getType());
                        assertNotNull(info);

                        TestingPackageAnnot annot=null;
                        PackageInfo currPackage = classInfo.getPackageInfo();
                        while(currPackage!=null){
                            annot = currPackage.getAnnotation(TestingPackageAnnot.class);
                            if(annot!=null){
                                break;
                            }
                            currPackage=currPackage.getParentPackage();
                        }
                        assertNotNull(annot);
                        assertEquals("toto2",annot.value());
                    }
                }
                Annotation[] annotArray = elemInfo.getAnnotations();
                Assert.assertEquals(1, annotArray.length);
                for(Annotation annot : annotArray){
                    AbstractClassInfo annotInfo = AbstractClassInfo.getClassInfo(annot.annotationType());
                    if(annotInfo.getSimpleName().equals("TestingAnnotation")){
                        Assert.assertEquals(1,annotInfo.getDeclaredMethods().size());
                        Assert.assertEquals("String",annotInfo.getDeclaredMethod("value").getReturnType().getMainType().getSimpleName());
                    }
                }


                if(elemInfo instanceof InterfaceInfo){
                    List<InterfaceInfo> parentInterfaces= ((InterfaceInfo) elemInfo).getParentInterfaces();
                    printNote("Nb interfaces "+parentInterfaces.size());
                }
            }
            catch(AnnotationProcessorException e){
                throw new RuntimeException(e);
            }

            VelocityContext velocityContext = AnnotationProcessorVelocityEngine.newContext(velocityLogger);
            //velocityContext.internalGetKeys();
            try {
                AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo((TypeElement) baseElem);
                velocityContext.put("packageName",classInfo.getPackageInfo().getName());
                velocityContext.put("className",classInfo.getSimpleName());
                AnnotationProcessorVelocityEngine.createSource(processingEnv, velocityContext, "velocityClass.vm", classInfo.getName() + "Generated");
            }
            catch (ClassCastException e){
                throw new RuntimeException("Wrong type "+baseElem.toString());
            }
            catch(IOException e){
                throw new RuntimeException("Cannot generate element",e);
            }
        }

        return false;
    }

    public static class LocalClassInfo {
        //private final String _className;
        private final String packageName;
        private final String annotationValue;

        public LocalClassInfo(Elements elementUtils, Element element){
            TestingAnnotation annotation = element.getAnnotation(TestingAnnotation.class);
            annotationValue = annotation.value();
            packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            //element.
            //element.getAnnotation(TestingAnnotation.class);
        }
    }

}


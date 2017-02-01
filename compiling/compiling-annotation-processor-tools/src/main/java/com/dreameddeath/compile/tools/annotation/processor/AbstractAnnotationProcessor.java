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

package com.dreameddeath.compile.tools.annotation.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;

/**
 * Created by Christophe Jeunesse on 10/08/2015.
 */
public abstract class AbstractAnnotationProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        AnnotationElementType.CURRENT_ELEMENT_UTILS.set(processingEnv.getElementUtils());
    }

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public void printNote(String message){
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, this.getClass().getSimpleName()+": "+message);
    }

    public void error(String message){
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, this.getClass().getSimpleName()+": "+message);
    }
}

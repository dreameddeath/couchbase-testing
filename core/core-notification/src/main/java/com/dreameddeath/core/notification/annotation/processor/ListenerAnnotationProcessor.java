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

package com.dreameddeath.core.notification.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.AnnotationProcessFileUtils;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.annotation.EventOrigModelID;
import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.annotation.ListenerProcessor;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;
import com.google.common.base.Preconditions;
import io.reactivex.Single;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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
        "com.dreameddeath.core.notification.annotation.ListenerProcessor"
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

        for(Element methodElement:roundEnv.getElementsAnnotatedWith(ListenerProcessor.class)){

            MethodInfo methodInfo = MethodInfo.getMethodInfo((ExecutableElement) methodElement);

            Preconditions.checkArgument(methodInfo.isPublic(),"The method %s() isn't public",methodInfo.getFullName());
            AbstractClassInfo eventType = methodInfo.getMethodParameters().get(0).getMainType();
            Preconditions.checkArgument(eventType.isInstanceOf(IEvent.class),"The ListenerProcessor method %s.%s() doesn't have a IEvent class as first parameter",methodInfo.getFullName());
            Preconditions.checkArgument(eventType.isInstanceOf(Event.class) || eventType.getAnnotation(EventOrigModelID.class)!=null,"The ListenerProcessor method %s.%s() doesn't have an explicit model id for event class %s",methodInfo.getFullName(),eventType.getFullName());

            if(methodInfo.getMethodParameters().size()==2) {
                Preconditions.checkArgument(methodInfo.getReturnType().getMainType().isInstanceOf(Single.class) && methodInfo.getReturnType().getMainTypeGeneric(0).getMainType().isInstanceOf(AbstractNotificationProcessor.ProcessingResult.class), "The method %s() isn't of type Single<ProcessingResult>", methodInfo.getFullName());
                Preconditions.checkArgument(methodInfo.getMethodParameters().get(1).getMainType().isInstanceOf(ICouchbaseSession.class), "The Listener processor method %s() doesn't have a CouchbaseSession class as second parameter", methodInfo.getFullName());
            }
            else {
                Preconditions.checkArgument(methodInfo.getReturnType().getMainType().isInstanceOf(Single.class) && methodInfo.getReturnType().getMainTypeGeneric(0).getMainType().isInstanceOf(AbstractNotificationProcessor.ProcessingResultInfo.class), "The method %s() isn't of type Single<ProcessingResultInfo>", methodInfo.getFullName());
                Preconditions.checkArgument(methodInfo.getMethodParameters().size() == 3, "The Listener processor method %s() doesn't have the right number of parameters", methodInfo.getFullName());
                Preconditions.checkArgument(methodInfo.getMethodParameters().get(1).getMainType().isInstanceOf(Notification.class), "The Listener processor method %s() doesn't have a Notification class as second parameter", methodInfo.getFullName());
                Preconditions.checkArgument(methodInfo.getMethodParameters().get(2).getMainType().isInstanceOf(ICouchbaseSession.class), "The Listener processor method %s() doesn't have a CouchbaseSession class as third parameter", methodInfo.getFullName());
            }
        }

        return true;
    }
}

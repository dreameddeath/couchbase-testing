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

package com.dreameddeath.core.query.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.dto.converter.DtoConverterFactory;
import com.dreameddeath.core.model.dto.converter.IDtoOutputConverter;
import com.dreameddeath.core.query.annotation.DtoModelQueryRestApi;
import com.dreameddeath.core.query.annotation.RemoteQueryInfo;
import com.dreameddeath.core.query.service.AbstractStandardQueryService;
import com.dreameddeath.core.query.service.rest.AbstractRestQueryService;
import com.dreameddeath.core.service.annotation.DataAccessType;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.*;
import io.swagger.annotations.Api;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Generated;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.ws.rs.Path;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
@SupportedAnnotationTypes(
    {"com.dreameddeath.core.query.annotation.DtoModelQueryRestApi"}
)
public class QueryServiceProcessor extends AbstractAnnotationProcessor{
    private static final Logger LOG = LoggerFactory.getLogger(QueryServiceProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        for(Element localEntityElement:roundEnv.getElementsAnnotatedWith(DtoModelQueryRestApi.class)){
            try{
                AbstractClassInfo dtoModelClassInfo=AbstractClassInfo.getClassInfo((TypeElement)localEntityElement);
                DtoModelQueryRestApi annot = dtoModelClassInfo.getAnnotation(DtoModelQueryRestApi.class);
                AbstractClassInfo origClassInfo = ClassInfo.getClassInfo(annot.baseClass());
                JavaFile internalServiceFile = generateInternalService(dtoModelClassInfo,origClassInfo);
                writeFile(internalServiceFile,messager);
                ClassName internServiceClassName = ClassName.get(internalServiceFile.packageName,internalServiceFile.typeSpec.name);
                JavaFile restServiceFile = generateRestService(internServiceClassName,dtoModelClassInfo,origClassInfo);
                writeFile(restServiceFile,messager);
            }
            catch(ClassNotFoundException e){
                LOG.error("Error during processing",e);
                StringBuilder buf = new StringBuilder();
                for(StackTraceElement elt:e.getStackTrace()){
                    buf.append(elt.toString());
                    buf.append("\n");
                }
                messager.printMessage(Diagnostic.Kind.ERROR,"Error during processing "+e.getMessage()+"\n"+buf.toString());
                throw new RuntimeException("Error during annotation processor",e);
            }
        }
        return true;
    }

    private JavaFile generateRestService(ClassName internServiceClassName, AbstractClassInfo dtoModelClassInfo, AbstractClassInfo origClassInfo) {
        DtoModelQueryRestApi annot = dtoModelClassInfo.getAnnotation(DtoModelQueryRestApi.class);
        String packageName = origClassInfo.getPackageInfo().getName().replaceAll("\\.model\\b",".service.rest");
        String serviceClassName ="RestQuery"+origClassInfo.getSimpleName()+"Service";

        RemoteQueryInfo restQueryInfo = dtoModelClassInfo.getAnnotation(RemoteQueryInfo.class);
        Preconditions.checkArgument(restQueryInfo!=null, "The type %s must have a %s annot", dtoModelClassInfo.getFullName(),RemoteQueryInfo.class.getCanonicalName());
        return JavaFile.builder(
                packageName,
                TypeSpec.classBuilder(serviceClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(
                                AnnotationSpec.builder(Generated.class)
                                        .addMember("value","$S",this.getClass().getName())
                                        .addMember("date","$S" , LocalDateTime.now().toString())
                                        .build()
                        )
                        .addAnnotation(
                                AnnotationSpec.builder(ServiceDef.class)
                                        .addMember("type","$S",AbstractRestQueryService.SERVICE_TYPE)
                                        .addMember("domain","$S",restQueryInfo.domain())
                                        .addMember("name","$S" ,restQueryInfo.name())
                                        .addMember("version","$S" ,restQueryInfo.version())
                                        .addMember("access","$T.$L", DataAccessType.class,DataAccessType.READ_ONLY.name())
                                        .addMember("status","$T.$L", com.dreameddeath.core.service.annotation.VersionStatus.class,annot.status().name())
                                        .build()
                        )
                        .addAnnotation(
                                AnnotationSpec.builder(Path.class)
                                        .addMember("value","$S",annot.rootPath())
                                        .build()
                        )
                        .addAnnotation(
                                AnnotationSpec.builder(Api.class)
                                        .addMember("value","$S",annot.rootPath())
                                        .build()
                        )
                        .superclass(
                                ParameterizedTypeName.get(
                                        ClassName.get(AbstractRestQueryService.class),
                                        dtoModelClassInfo.getClassName()
                                )
                        )
                        .addField(FieldSpec.builder(internServiceClassName, "queryService",Modifier.PRIVATE).build())
                        .addMethod(
                                MethodSpec.methodBuilder("setQueryService")
                                        .addAnnotation(Autowired.class)
                                        .addParameter(internServiceClassName,"queryService")
                                        .addStatement("this.queryService=queryService")
                                        .build()
                        )
                        .addMethod(
                                MethodSpec.methodBuilder("getQueryService")
                                        .addModifiers(Modifier.PROTECTED)
                                        .addAnnotation(Override.class)
                                        .returns(internServiceClassName)
                                        .addStatement("return queryService")
                                        .build()
                        )
                        .build()
        ).build();
    }

    private JavaFile generateInternalService(AbstractClassInfo dtoModelClassInfo, AbstractClassInfo origClassInfo) {
        DtoModelQueryRestApi annot = dtoModelClassInfo.getAnnotation(DtoModelQueryRestApi.class);
        String packageName = origClassInfo.getPackageInfo().getName().replaceAll("\\.model\\b",".service");
        String serviceClassName ="Query"+origClassInfo.getSimpleName()+"Service";

        TypeName outConverterType = ParameterizedTypeName.get(ClassName.get(IDtoOutputConverter.class),
                origClassInfo.getClassName(),
                dtoModelClassInfo.getClassName()
        );

        return JavaFile.builder(
                packageName,
                TypeSpec.classBuilder(serviceClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(
                                AnnotationSpec.builder(Generated.class)
                                        .addMember("value","$S",this.getClass().getName())
                                        .addMember("date","$S" , LocalDateTime.now().toString())
                                        .build()
                        )
                        .superclass(
                                ParameterizedTypeName.get(
                                        ClassName.get(AbstractStandardQueryService.class),
                                        origClassInfo.getClassName(),
                                        dtoModelClassInfo.getClassName()
                                )
                        )
                        .addField(FieldSpec.builder(outConverterType, "outputConverter",Modifier.PRIVATE).build())
                        .addMethod(
                                MethodSpec.methodBuilder("setDtoConverterFactory")
                                        .addAnnotation(Autowired.class)
                                        .addParameter(DtoConverterFactory.class,"factory")
                                        .addStatement("outputConverter=factory.getDtoOutputConverter($T.class,$T.class)",origClassInfo.getClassName(),dtoModelClassInfo.getClassName())
                                        .build()
                        )
                        .addMethod(
                                MethodSpec.methodBuilder("getOutputConverter")
                                        .addModifiers(Modifier.PROTECTED)
                                        .addAnnotation(Override.class)
                                        .returns(outConverterType)
                                        .addStatement("return outputConverter")
                                        .build()
                        )
                        .build()
        ).build();
    }
}

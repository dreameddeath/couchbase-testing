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

package com.dreameddeath.couchbase.core.process.remote.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.AbstractAnnotationProcessor;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.converter.DtoConverterFactory;
import com.dreameddeath.core.model.dto.converter.IDtoInputConverter;
import com.dreameddeath.core.model.dto.converter.IDtoOutputConverter;
import com.dreameddeath.core.service.annotation.DataAccessType;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.couchbase.core.process.remote.annotation.DtoModelRestApi;
import com.dreameddeath.couchbase.core.process.remote.annotation.RemoteServiceInfo;
import com.dreameddeath.couchbase.core.process.remote.model.rest.ActionRequest;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.couchbase.core.process.remote.service.AbstractRemoteJobRestService;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.joda.time.LocalDate;
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
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
@SupportedAnnotationTypes(
        {"com.dreameddeath.couchbase.core.process.remote.annotation.DtoModelRestApi"}
)
public class ProcessRestServiceProcessor extends AbstractAnnotationProcessor{
    private static final Logger LOG = LoggerFactory.getLogger(ProcessRestServiceProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        Map<String,ClassInfo> dtoInputModelMap = new HashMap<>();
        Map<String,ClassInfo> dtoOutputModelMap = new HashMap<>();
        Set<String> classNames=new HashSet<>();

        for(Element classElem : roundEnv.getElementsAnnotatedWith(DtoModelRestApi.class)) {
            try{
                ClassInfo classInfo = (ClassInfo)ClassInfo.getClassInfo((TypeElement)classElem);
                DtoModelRestApi modelRestApi = classInfo.getAnnotation(DtoModelRestApi.class);
                classNames.add(modelRestApi.baseClass());
                if(modelRestApi.mode()== DtoInOutMode.IN){
                    dtoInputModelMap.put(modelRestApi.baseClass(),classInfo);
                }
                else{
                    dtoOutputModelMap.put(modelRestApi.baseClass(),classInfo);
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
        }

        for(String origClassName:classNames){
            Preconditions.checkArgument(dtoInputModelMap.containsKey(origClassName),"No input model found for orig className %s",origClassName);
            Preconditions.checkArgument(dtoOutputModelMap.containsKey(origClassName),"No output model found for orig className %s",origClassName);
            JavaFile javaFile = generateRemoteServiceInfo(dtoInputModelMap.get(origClassName),dtoOutputModelMap.get(origClassName));
            try {
                messager.printMessage(Diagnostic.Kind.NOTE,"Generating "+javaFile.packageName+"."+javaFile.typeSpec.name);
                javaFile.writeTo(processingEnv.getFiler());
            }
            catch(IOException e){
                LOG.error("Error during processing",e);
                StringBuffer buf = new StringBuffer();
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

    public JavaFile generateRemoteServiceInfo(ClassInfo inDtoModelClassInfo, ClassInfo outDtoModelClassInfo){
        DtoModelRestApi inRestApi = inDtoModelClassInfo.getAnnotation(DtoModelRestApi.class);
        DtoModelRestApi outRestApi = outDtoModelClassInfo.getAnnotation(DtoModelRestApi.class);
        checkValueConsistency(inRestApi.baseClass(),outRestApi.baseClass(),inDtoModelClassInfo,outDtoModelClassInfo,"DtoModelRestApi.baseClass");
        checkValueConsistency(inRestApi.rootPath(),outRestApi.rootPath(),inDtoModelClassInfo,outDtoModelClassInfo,"DtoModelRestApi.rootPath");
        checkValueConsistency(inRestApi.status(),outRestApi.status(),inDtoModelClassInfo,outDtoModelClassInfo,"DtoModelRestApi.status");
        checkValueConsistency(inRestApi.version(),outRestApi.version(),inDtoModelClassInfo,outDtoModelClassInfo,"DtoModelRestApi.version");

        RemoteServiceInfo inRemoteServiceInfo= inDtoModelClassInfo.getAnnotation(RemoteServiceInfo.class);
        RemoteServiceInfo outRemoteServiceInfo= outDtoModelClassInfo.getAnnotation(RemoteServiceInfo.class);
        checkValueConsistency(inRemoteServiceInfo.domain(),outRemoteServiceInfo.domain(),inDtoModelClassInfo,outDtoModelClassInfo,"RemoteServiceInfo.domain");
        checkValueConsistency(inRemoteServiceInfo.name(),outRemoteServiceInfo.name(),inDtoModelClassInfo,outDtoModelClassInfo,"RemoteServiceInfo.name");
        checkValueConsistency(inRemoteServiceInfo.version(),outRemoteServiceInfo.version(),inDtoModelClassInfo,outDtoModelClassInfo,"RemoteServiceInfo.version");

        try {
            ClassInfo baseClass = (ClassInfo) ClassInfo.getClassInfo(inRestApi.baseClass());

            TypeName inConverterType = ParameterizedTypeName.get(ClassName.get(IDtoInputConverter.class),
                    baseClass.getClassName(),
                    inDtoModelClassInfo.getClassName()
            );

            TypeName outConverterType = ParameterizedTypeName.get(ClassName.get(IDtoOutputConverter.class),
                    baseClass.getClassName(),
                    outDtoModelClassInfo.getClassName()
            );
            String packageName = baseClass.getPackageInfo().getName().replaceAll("\\.model\\b",".service.rest");
            String serviceClassName ="Remote"+baseClass.getSimpleName()+"Service";
            String wrapperClassName = "WrappedResponse"+outDtoModelClassInfo.getSimpleName();
            return JavaFile.builder(
                    packageName,
                    TypeSpec.classBuilder(serviceClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(
                            AnnotationSpec.builder(Generated.class)
                                .addMember("value","$S",this.getClass().getName())
                                .addMember("date","$S" ,LocalDate.now().toString())
                                .build()
                    )
                    .addAnnotation(
                            AnnotationSpec.builder(ServiceDef.class)
                                .addMember("type","$S",AbstractRemoteJobRestService.SERVICE_TYPE)
                                .addMember("domain","$S",inRemoteServiceInfo.domain())
                                .addMember("name","$S" ,inRemoteServiceInfo.name())
                                .addMember("version","$S" ,inRemoteServiceInfo.version())
                                .addMember("access","$T.$L", DataAccessType.class,DataAccessType.READ_WRITE.name())
                                .addMember("status","$T.$L", com.dreameddeath.core.service.annotation.VersionStatus.class,inRestApi.status().name())
                                .build()
                    )
                    .addAnnotation(
                            AnnotationSpec.builder(Path.class)
                                .addMember("value","$S",inRestApi.rootPath())
                                .build()
                    )
                    .addAnnotation(
                            AnnotationSpec.builder(Api.class)
                                .addMember("value","$S",inRestApi.rootPath())
                                .build()
                    )
                    .superclass(
                            ParameterizedTypeName.get(
                                ClassName.get(AbstractRemoteJobRestService.class),
                                baseClass.getClassName(),
                                inDtoModelClassInfo.getClassName(),
                                outDtoModelClassInfo.getClassName()
                            )
                    )
                    .addField(FieldSpec.builder(inConverterType,"inputConverter", Modifier.PRIVATE).build())
                    .addField(FieldSpec.builder(outConverterType, "outputConverter",Modifier.PRIVATE).build())
                    .addMethod(
                            MethodSpec.methodBuilder("setDtoConverterFactory")
                                .addAnnotation(Autowired.class)
                                .addParameter(DtoConverterFactory.class,"factory")
                                .addStatement("inputConverter=factory.getDtoInputConverter($T.class,$T.class)",baseClass.getClassName(),inDtoModelClassInfo.getClassName())
                                .addStatement("outputConverter=factory.getDtoOutputConverter($T.class,$T.class)",baseClass.getClassName(),outDtoModelClassInfo.getClassName())
                                .build()

                    )
                    .addMethod(
                            MethodSpec.methodBuilder("getInputConverter")
                                    .addModifiers(Modifier.PROTECTED)
                                    .addAnnotation(Override.class)
                                    .returns(inConverterType)
                                    .addStatement("return inputConverter")
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
                    .addType(
                            TypeSpec.classBuilder(wrapperClassName)
                                .addModifiers(Modifier.STATIC,Modifier.PUBLIC)
                                .superclass(
                                        ParameterizedTypeName.get(ClassName.get(RemoteJobResultWrapper.class),
                                                outDtoModelClassInfo.getClassName()
                                                )
                                )
                                .addMethod(MethodSpec.constructorBuilder()
                                    .addParameter(outDtoModelClassInfo.getClassName(),"result")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addStatement("super(result)")
                                    .build()
                                )
                            .build()
                    )
                    .addMethod(
                            MethodSpec.methodBuilder("getResponseClass")
                                    .addModifiers(Modifier.PROTECTED)
                                    .addAnnotation(Override.class)
                                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class),ClassName.get(packageName,serviceClassName,wrapperClassName)))
                                    .addStatement("return $L.class",wrapperClassName)
                                    .build()
                    )
                    /*
                    *  POST Job Method
                    */
                    .addMethod(
                            MethodSpec.methodBuilder("runJobCreate")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(POST.class)
                                .addAnnotation(
                                    AnnotationSpec.builder(Produces.class)
                                        .addMember("value","{$T.$L}", ClassName.get(MediaType.class), "APPLICATION_JSON")
                                        .build()
                                )
                                .addAnnotation(
                                    AnnotationSpec.builder(ApiResponses.class)
                                        .addMember("value","$L",
                                            AnnotationSpec.builder(ApiResponse.class)
                                                .addMember("code","$L",200)
                                                .addMember("message","$S","The processing result")
                                                .addMember("response","$L.class",wrapperClassName)
                                                .build()
                                        )
                                        .addMember("value","$L",
                                            AnnotationSpec.builder(ApiResponse.class)
                                                .addMember("code","$L",409)
                                                .addMember("message","$S","Conflict with request uid")
                                                .build()
                                        )
                                        .build()
                                )
                                .addParameter(ParameterSpec.builder(IUser.class,"user")
                                        .addAnnotation(Context.class).build())
                                .addParameter(ParameterSpec.builder(Boolean.class,"submitOnly")
                                        .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                                        .addMember("value","$T.$L",AbstractRemoteJobRestService.class,"SUBMIT_ONLY_QUERY_PARAM")
                                        .build()).build())
                                .addParameter(ParameterSpec.builder(String.class,"requestUid")
                                        .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                                            .addMember("value","$T.$L",AbstractRemoteJobRestService.class,"REQUEST_UID_QUERY_PARAM")
                                            .build())
                                        .build()
                                )
                                .addParameter(ParameterSpec.builder(inDtoModelClassInfo.getClassName(),"request").build())
                                .addParameter(ParameterSpec.builder(AsyncResponse.class,"asyncResponse")
                                            .addModifiers(Modifier.FINAL)
                                            .addAnnotation(Suspended.class).build()
                                )
                                .addStatement("super.doRunJobCreate(user,submitOnly,requestUid,request,asyncResponse)")
                                .build()
                    )//End of runJob / POST job
                    .addMethod(
                            MethodSpec.methodBuilder("getJob")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addAnnotation(GET.class)
                                    .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value","$S","/{uid}").build())
                                    .addAnnotation(
                                            AnnotationSpec.builder(Produces.class)
                                                    .addMember("value","{$T.$L}", ClassName.get(MediaType.class), "APPLICATION_JSON")
                                                    .build()
                                    )
                                    .addAnnotation(
                                            AnnotationSpec.builder(ApiResponses.class)
                                                    .addMember("value","$L",
                                                            AnnotationSpec.builder(ApiResponse.class)
                                                                    .addMember("code","$L",200)
                                                                    .addMember("message","$S","The processing result")
                                                                    .addMember("response","$L.class",wrapperClassName)
                                                                    .build()
                                                    )
                                                    .addMember("value","$L",
                                                            AnnotationSpec.builder(ApiResponse.class)
                                                                    .addMember("code","$L",404)
                                                                    .addMember("message","$S","Job not found")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .addParameter(ParameterSpec.builder(IUser.class,"user")
                                            .addAnnotation(Context.class).build())
                                    .addParameter(ParameterSpec.builder(String.class,"uid")
                                            .addAnnotation(AnnotationSpec.builder(PathParam.class)
                                                    .addMember("value","$S","uid")
                                                    .build()).build())
                                    .addParameter(ParameterSpec.builder(AsyncResponse.class,"asyncResponse")
                                            .addModifiers(Modifier.FINAL)
                                            .addAnnotation(Suspended.class).build()
                                    )
                                    .addStatement("super.doGetJob(user,uid,asyncResponse)")
                                    .build()
                    )//End of getJob / GET /{jobId}
                    .addMethod(
                            MethodSpec.methodBuilder("updateJob")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addAnnotation(PUT.class)
                                    .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value","$S","/{uid}/{action:cancel|resume}").build())
                                    .addAnnotation(
                                            AnnotationSpec.builder(Produces.class)
                                                    .addMember("value","{$T.$L}", ClassName.get(MediaType.class), "APPLICATION_JSON")
                                                    .build()
                                    )
                                    .addAnnotation(
                                            AnnotationSpec.builder(ApiResponses.class)
                                                    .addMember("value","$L",
                                                            AnnotationSpec.builder(ApiResponse.class)
                                                                    .addMember("code","$L",200)
                                                                    .addMember("message","$S","The processing result")
                                                                    .addMember("response","$L.class",wrapperClassName)
                                                                    .build()
                                                    )
                                                    .addMember("value","$L",
                                                            AnnotationSpec.builder(ApiResponse.class)
                                                                    .addMember("code","$L",404)
                                                                    .addMember("message","$S","Job not found")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .addParameter(ParameterSpec.builder(IUser.class,"user")
                                            .addAnnotation(Context.class).build())
                                    .addParameter(ParameterSpec.builder(String.class,"uid")
                                            .addAnnotation(AnnotationSpec.builder(PathParam.class)
                                                    .addMember("value","$S","uid")
                                                    .build()).build())
                                    .addParameter(ParameterSpec.builder(String.class,"requestUid")
                                            .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                                                    .addMember("value","$T.$L",AbstractRemoteJobRestService.class,"REQUEST_UID_QUERY_PARAM")
                                                    .build())
                                            .build()
                                    )
                                    .addParameter(ParameterSpec.builder(ActionRequest.class,"actionRequest")
                                            .addAnnotation(AnnotationSpec.builder(PathParam.class)
                                                    .addMember("value","$S","action")
                                                    .build()).build())
                                    .addParameter(ParameterSpec.builder(AsyncResponse.class,"asyncResponse")
                                            .addModifiers(Modifier.FINAL)
                                            .addAnnotation(Suspended.class).build()
                                    )
                                    .addStatement("super.doUpdateJob(user,uid,requestUid,actionRequest,asyncResponse)")
                                    .build()
                    )//End of updateJob / PUT job
                    .build()
                ).build();//End of
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException(e);
        }

    }


    private <T> void checkValueConsistency(T inValue, T outValue, ClassInfo inInfo, ClassInfo outInfo, String attributeName){
        Preconditions.checkArgument(Objects.equals(inValue,outValue),
                "Inconsistent %s annot value (<%s> vs <%s>) for classes <%s>/<%s>",
                    attributeName,
                    inValue,
                    outValue,
                    inInfo.getFullName(),
                    outInfo.getFullName()
                );
    }
}

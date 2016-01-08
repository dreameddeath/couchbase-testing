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

package com.dreameddeath.core.service.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ParameterizedTypeInfo;
import com.dreameddeath.core.service.annotation.BodyInfo;
import com.dreameddeath.core.service.annotation.ExposeMethod;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextFactory;
import com.dreameddeath.core.service.context.provider.GlobalContextProvider;
import com.dreameddeath.core.service.context.provider.UserContextProvider;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 03/04/2015.
 */
public class ServiceExpositionMethodDef {
    private String name;
    private String httpMethod;
    private AbstractClassInfo returnType;
    private String globalContextParamName =null;
    private String userParamName =null;
    private VersionStatus status;
    private ServiceExpositionPathInfo pathInfo;
    private List<ServiceExpositionMethodParamDefinition> methodParamsDefinition =new ArrayList<>();
    private ServiceExpositionMethodBodyDef bodyInfo = null;


    public ServiceExpositionMethodDef(MethodInfo methodInfo){
        ExposeMethod exposeMethodAnnot=methodInfo.getAnnotation(ExposeMethod.class);
        name = methodInfo.getName();
        httpMethod = exposeMethodAnnot.method();
        status = exposeMethodAnnot.status();
        pathInfo = new ServiceExpositionPathInfo(exposeMethodAnnot.path(),methodInfo);
        returnType = methodInfo.getReturnType().getMainTypeGeneric(0).getMainType();

        for(ParameterizedTypeInfo paramInfo: methodInfo.getMethodParameters()){
            ServiceExpositionMethodParamDefinition methodParam = new ServiceExpositionMethodParamDefinition(paramInfo,pathInfo);
            methodParamsDefinition.add(methodParam);
            if(methodParam.isGlobalContextParam()){
                globalContextParamName = methodParam.getName();
            }
            if(methodParam.isUserParam()){
                userParamName = methodParam.getName();
            }
        }

        BodyInfo bodyInfoAnnot = methodInfo.getAnnotation(BodyInfo.class);
        if(bodyInfoAnnot!=null) {
            bodyInfo = new ServiceExpositionMethodBodyDef(bodyInfoAnnot.paramName(),methodInfo);
        }
        else{
            bodyInfo =null;
        }
    }

    public String getName(){
        return name;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public List<ServiceExpositionMethodParamDefinition> getMethodParamsDefinition() {
        return Collections.unmodifiableList(methodParamsDefinition);
    }

    public ServiceExpositionPathInfo getPathInfo() {
        return pathInfo;
    }

    public boolean hasGlobalContextParam() {
        return globalContextParamName !=null;
    }

    public String getGlobalContextParamName(){
        return globalContextParamName;
    }

    public boolean hasUserParam(){ return userParamName!=null;}

    public String getUserParamName(){return userParamName;}

    public VersionStatus getStatus() {
        return status;
    }

    public String getReturnClassName() {
        return returnType.getSimpleName();
    }

    public Set<String> getImports(){
        Set<String> result = new TreeSet<>();
        if(hasGlobalContextParam()){
            result.add(AbstractClassInfo.getClassInfo(IGlobalContext.class).getImportName());
            result.add(AbstractClassInfo.getClassInfo(IGlobalContextFactory.class).getImportName());
            result.add(AbstractClassInfo.getClassInfo(GlobalContextProvider.class).getImportName());
        }
        if(hasUserParam()){
            result.add(AbstractClassInfo.getClassInfo(IUser.class).getImportName());
            result.add(AbstractClassInfo.getClassInfo(IUserFactory.class).getImportName());
            result.add(AbstractClassInfo.getClassInfo(UserContextProvider.class).getImportName());
        }
        result.add(returnType.getImportName());
        for(ServiceExpositionMethodParamDefinition methodParam:methodParamsDefinition){
            result.add(methodParam.getImportName());
        }
        result.addAll(pathInfo.getImports());
        if(bodyInfo!=null){
            result.add(bodyInfo.getImportName());
        }
        return result;
    }

    public boolean hasBody() {
        return bodyInfo !=null;
    }

    public boolean needEmptyBody(){
        return (httpMethod.equalsIgnoreCase("post")||httpMethod.equalsIgnoreCase("put"));
    }
    public ServiceExpositionMethodBodyDef getBodyInfo() {
        return bodyInfo;
    }
}

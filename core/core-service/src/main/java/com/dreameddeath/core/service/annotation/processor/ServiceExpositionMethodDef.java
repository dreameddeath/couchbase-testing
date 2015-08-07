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
import com.dreameddeath.core.service.context.IGlobalContextTranscoder;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 03/04/2015.
 */
public class ServiceExpositionMethodDef {
    private String _name;
    private String _httpMethod;
    private AbstractClassInfo _returnType;
    private String _globalContextParamName =null;
    private VersionStatus _status;
    private ServiceExpositionPathInfo _pathInfo;
    private List<ServiceExpositionMethodParamDefinition> _methodParamsDefinition =new ArrayList<>();
    private ServiceExpositionMethodBodyDef _bodyInfo = null;


    public ServiceExpositionMethodDef(MethodInfo methodInfo){
        ExposeMethod exposeMethodAnnot=methodInfo.getAnnotation(ExposeMethod.class);
        _name = methodInfo.getName();
        _httpMethod = exposeMethodAnnot.method();
        _status = exposeMethodAnnot.status();
        _pathInfo = new ServiceExpositionPathInfo(exposeMethodAnnot.path(),methodInfo);

        _returnType = methodInfo.getReturnType().getMainTypeGeneric(0).getMainType();

        for(ParameterizedTypeInfo paramInfo: methodInfo.getMethodParameters()){
            ServiceExpositionMethodParamDefinition methodParam = new ServiceExpositionMethodParamDefinition(paramInfo,_pathInfo);
            _methodParamsDefinition.add(methodParam);
            if(methodParam.isGlobalContextParam()){
                _globalContextParamName = methodParam.getName();
            }
        }


        BodyInfo bodyInfoAnnot = methodInfo.getAnnotation(BodyInfo.class);
        if(bodyInfoAnnot!=null) {
            _bodyInfo = new ServiceExpositionMethodBodyDef(bodyInfoAnnot.paramName(),methodInfo);
        }
        else{
            _bodyInfo =null;
        }

    }

    public String getName(){
        return _name;
    }

    public String getHttpMethod() {
        return _httpMethod;
    }

    public List<ServiceExpositionMethodParamDefinition> getMethodParamsDefinition() {
        return Collections.unmodifiableList(_methodParamsDefinition);
    }

    public ServiceExpositionPathInfo getPathInfo() {
        return _pathInfo;
    }

    public boolean hasGlobalContextParam() {
        return _globalContextParamName !=null;
    }

    public String getGlobalContextParamName(){
        return _globalContextParamName;
    }

    public VersionStatus getStatus() {
        return _status;
    }

    public String getReturnClassName() {
        return _returnType.getSimpleName();
    }

    public Set<String> getImports(){
        Set<String> result = new TreeSet<>();
        if(hasGlobalContextParam()){
            result.add(AbstractClassInfo.getClassInfo(IGlobalContext.class).getImportName());
            result.add(AbstractClassInfo.getClassInfo(IGlobalContextTranscoder.class).getImportName());
        }
        result.add(_returnType.getImportName());
        for(ServiceExpositionMethodParamDefinition methodParam:_methodParamsDefinition){
            result.add(methodParam.getImportName());
        }
        result.addAll(_pathInfo.getImports());
        if(_bodyInfo!=null){
            result.add(_bodyInfo.getImportName());
        }
        return result;
    }

    public boolean hasBody() {
        return _bodyInfo !=null;
    }

    public boolean needEmptyBody(){
        return (_httpMethod.equalsIgnoreCase("post")||_httpMethod.equalsIgnoreCase("put"));
    }
    public ServiceExpositionMethodBodyDef getBodyInfo() {
        return _bodyInfo;
    }
}

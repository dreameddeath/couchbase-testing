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

import com.dreameddeath.compile.tools.annotation.processor.reflection.ParameterizedTypeInfo;
import com.dreameddeath.core.service.context.IGlobalContext;

/**
 * Created by Christophe Jeunesse on 07/04/2015.
 */
public class ServiceExpositionMethodParamDefinition {
    private boolean _isGlobalContextParam;
    private ParameterizedTypeInfo _paramInfo;

    public ServiceExpositionMethodParamDefinition(ParameterizedTypeInfo paramInfo, ServiceExpositionPathInfo pathInfo){
        _isGlobalContextParam = paramInfo.isAssignableTo(IGlobalContext.class);
        _paramInfo = paramInfo;
    }

    public boolean isGlobalContextParam() {
        return _isGlobalContextParam;
    }

    public String getName(){
        return _paramInfo.getName();
    }

    public String getClassName(){
        return _paramInfo.getMainType().getSimpleName();
    }

    public String getPackage(){
        return _paramInfo.getMainType().getPackageInfo().getName();
    }

    public String getImportName(){
        return _paramInfo.getMainType().getImportName();
    }
}

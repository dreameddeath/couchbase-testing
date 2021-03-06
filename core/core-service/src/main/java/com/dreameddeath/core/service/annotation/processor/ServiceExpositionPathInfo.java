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

import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 08/04/2015.
 */
public class ServiceExpositionPathInfo {
    private String jaxRsPath = "";
    private String jaxRsPattern = "";
    private List<ServiceExpositionParamInfo> urlParamsList = new ArrayList<>();
    private List<ServiceExpositionParamInfo> queryParamsList = new ArrayList<>();

    public ServiceExpositionPathInfo(String path, MethodInfo methodInfo) {
        String[] uriParts = path.split("\\?"); //Split path and Query Part
        String[] pathParts = uriParts[0].split("/");
        String[] queryParams;
        if (uriParts.length > 1) {
            queryParams = uriParts[1].split("&");
        } else {
            queryParams = new String[]{};
        }
        for (String pathPart : pathParts) {
            if((pathPart==null)|| pathPart.equals("")){
                continue;
            }
            if (pathPart.matches("^\\w+$")) {
                jaxRsPath += "/" + pathPart;
                jaxRsPattern+= "/"+pathPart;
            } else {
                ServiceExpositionParamInfo paramInfo = new ServiceExpositionParamInfo(false, pathPart, methodInfo);
                jaxRsPath += "/{" + paramInfo.getName() + "}";
                jaxRsPattern+="/"+paramInfo.getPatternFormat();
                urlParamsList.add(paramInfo);
            }
        }

        for (String queryParam : queryParams) {
            queryParamsList.add(new ServiceExpositionParamInfo(true, queryParam, methodInfo));
        }
    }

    public String getJaxRsPath() {
        return jaxRsPath;
    }

    public String getJaxRsPattern() {
        return jaxRsPattern;
    }

    public List<ServiceExpositionParamInfo> getUrlParamsList() {
        return Collections.unmodifiableList(urlParamsList);
    }

    public List<ServiceExpositionParamInfo> getQueryParamsList() {
        return Collections.unmodifiableList(queryParamsList);
    }

    public Set<String> getImports(){
        Set<String> result = new TreeSet<>();
        for(ServiceExpositionParamInfo paramInfo: urlParamsList){
            result.add(paramInfo.getImportName());
        }
        return result;
    }
}

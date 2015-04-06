/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.service.annotation.processor;

import com.dreameddeath.core.service.annotation.ExposeService;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.tools.annotation.processor.reflection.AbstractClassInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceaj8230 on 03/04/2015.
 */
public class ServiceExpositionDef {
    private String _package;
    private String _className;
    private List<String> _interfaces = new ArrayList<>();
    private String _path;
    private String _name;
    private String _version;
    private VersionStatus _status;

    private List<ServiceMethodExpositionDef> _methodList = new ArrayList<>();

    public ServiceExpositionDef(AbstractClassInfo classInfo){
        ExposeService serviceInfosAnnot = classInfo.getAnnotation(ExposeService.class);
        _package = classInfo.getPackageInfo().getName();
        _className = classInfo.getSimpleName();
        classInfo.getParentInterfaces().forEach(it->_interfaces.add(it.getFullName()));
        _path = serviceInfosAnnot.path();
        _name = serviceInfosAnnot.name();
        _version = serviceInfosAnnot.version();
        _status = serviceInfosAnnot.status();

        classInfo.getDeclaredMethods().stream()
                .filter(info -> info.getAnnotation(ExposeService.class) != null)
                .map(method -> _methodList.add(new ServiceMethodExpositionDef(method)));
    }

    public String getPath(){
        return _path;
    }

    public String getPackage() {
        return _package;
    }

    public String getName() {
        return _name;
    }

    public String getClassName() {
        return _className;
    }

    public List<String> getInterfaces() {
        return _interfaces;
    }
}

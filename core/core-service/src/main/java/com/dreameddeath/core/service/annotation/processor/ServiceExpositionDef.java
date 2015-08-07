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
import com.dreameddeath.core.service.annotation.ExposeMethod;
import com.dreameddeath.core.service.annotation.ExposeService;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.model.GeneratedRestImpl;
import com.dreameddeath.core.service.model.HasServiceClientFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 03/04/2015.
 */
public class ServiceExpositionDef {
    public static final String REST_CLIENT_SUFFIX = "RestClient";
    public static final String REST_SERVICE_SUFFIX = "RestService";

    public static<T> Class<GeneratedRestImpl<T>> getRestServerClass(Class<T> serviceClass) throws ClassNotFoundException{
        return (Class<GeneratedRestImpl<T>>)ServiceExpositionDef.class.getClassLoader().loadClass(serviceClass.getName()+REST_SERVICE_SUFFIX);
    }

    public static <T> GeneratedRestImpl<T> newRestServerIntance(T serviceImpl) throws ClassNotFoundException,InstantiationException,IllegalAccessException{
        GeneratedRestImpl<T> result = getRestServerClass((Class<T>)serviceImpl.getClass()).newInstance();
        result.setServiceImplementation(serviceImpl);
        return result;
    }

    public static <T> Class<T> getRestClientClass(Class serviceClass,Class<T> interfaceClass) throws ClassNotFoundException{
        return (Class<T>) ServiceExpositionDef.class.getClassLoader().loadClass(serviceClass.getName()+REST_CLIENT_SUFFIX);
    }

    public static <T> T getRestClientIntance(Class serviceClass,Class<T> interfaceClass,ServiceClientFactory factory) throws ClassNotFoundException,InstantiationException,IllegalAccessException{
        T result = getRestClientClass(serviceClass,interfaceClass).newInstance();
        ((HasServiceClientFactory)result).setServiceClientFactory(factory);
        return result;
    }

    private String _package;
    private String _className;
    private List<String> _interfaces = new ArrayList<>();
    private AbstractClassInfo _classInfo;
    private String _path;
    private String _name;
    private String _version;
    private VersionStatus _status;
    private Set<String> _imports = new TreeSet<>();
    private List<ServiceExpositionMethodDef> _methodList = new ArrayList<>();

    public ServiceExpositionDef(AbstractClassInfo classInfo){
        _classInfo = classInfo;
        ExposeService serviceInfosAnnot = classInfo.getAnnotation(ExposeService.class);
        _package = classInfo.getPackageInfo().getName();
        _className = classInfo.getSimpleName();
        _imports.add(classInfo.getImportName());
        classInfo.getParentInterfaces().forEach(it->{
            _imports.add(it.getImportName());
            _interfaces.add(it.getSimpleName());
        });
        _path = serviceInfosAnnot.path();
        _name = serviceInfosAnnot.name();
        _version = serviceInfosAnnot.version();
        _status = serviceInfosAnnot.status();

        classInfo.getDeclaredMethods().stream()
                .filter(methodInfo -> methodInfo.getAnnotation(ExposeMethod.class) != null)
                .map(method -> {
                    ServiceExpositionMethodDef methodDef = new ServiceExpositionMethodDef(method);
                    _imports.addAll(methodDef.getImports());
                    return _methodList.add(methodDef);
                })
                .collect(Collectors.toList());


    }

    public String getPath(){
        return _path;
    }

    public String getPackage() {
        return _package;
    }

    public VersionStatus getStatus() {
        return _status;
    }

    public String getName() {
        return _name;
    }

    public String getVersion() {
        return _version;
    }

    public String getClassName() {
        return _className;
    }

    public String getClientSimpleClassName(){
        return _classInfo.getSimpleName()+ REST_CLIENT_SUFFIX;
    }

    public String getServerSimpleClassName(){
        return _classInfo.getSimpleName()+ REST_SERVICE_SUFFIX;
    }

    public String getClientClassName(){
        return _classInfo.getFullName()+ REST_CLIENT_SUFFIX;
    }

    public String getServerClassName(){
        return _classInfo.getFullName()+ REST_SERVICE_SUFFIX;
    }


    public List<String> getInterfaces() {
        return Collections.unmodifiableList(_interfaces);
    }

    public List<ServiceExpositionMethodDef> getMethods() {
        return Collections.unmodifiableList(_methodList);
    }

    public boolean hasGlobalContextTranscoder(){
        return _methodList.stream().filter(ServiceExpositionMethodDef::hasGlobalContextParam).count()>0;
    }

    public Set<String> getImports() {
        return _imports;
    }


}

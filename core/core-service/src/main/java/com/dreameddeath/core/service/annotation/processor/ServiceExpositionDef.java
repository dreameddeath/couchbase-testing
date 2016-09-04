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
import com.dreameddeath.core.service.GeneratedRestImpl;
import com.dreameddeath.core.service.HasServiceClientFactory;
import com.dreameddeath.core.service.annotation.ExposeMethod;
import com.dreameddeath.core.service.annotation.ExposeService;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.client.AbstractServiceClientFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 03/04/2015.
 */
public class ServiceExpositionDef {
    public static final String REST_CLIENT_SUFFIX = "RestClient";
    public static final String REST_SERVICE_SUFFIX = "RestService";

    public static<T> Class<GeneratedRestImpl<T>> getRestServerClass(Class<T> serviceClass) throws ClassNotFoundException{
        return (Class<GeneratedRestImpl<T>>)Thread.currentThread().getContextClassLoader().loadClass(serviceClass.getName()+REST_SERVICE_SUFFIX);
    }

    public static <T> GeneratedRestImpl<T> newRestServerInstance(T serviceImpl) throws ClassNotFoundException,InstantiationException,IllegalAccessException{
        GeneratedRestImpl<T> result = getRestServerClass((Class<T>)serviceImpl.getClass()).newInstance();
        result.setServiceImplementation(serviceImpl);
        return result;
    }

    public static <T> Class<T> getRestClientClass(Class serviceClass,Class<T> interfaceClass) throws ClassNotFoundException{
        return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(serviceClass.getName()+REST_CLIENT_SUFFIX);
    }

    public static <T> T getRestClientIntance(Class serviceClass,Class<T> interfaceClass,AbstractServiceClientFactory factory) throws ClassNotFoundException,InstantiationException,IllegalAccessException{
        T result = getRestClientClass(serviceClass,interfaceClass).newInstance();
        ((HasServiceClientFactory)result).setServiceClientFactory(factory);
        return result;
    }

    private String packageName;
    private String className;
    private List<String> interfaces = new ArrayList<>();
    private AbstractClassInfo classInfo;
    private String path;
    private String domain;
    private String name;
    private String version;
    private VersionStatus status;
    private Set<String> imports = new TreeSet<>();
    private List<ServiceExpositionMethodDef> methodList = new ArrayList<>();

    public ServiceExpositionDef(AbstractClassInfo classInfo){
        this.classInfo = classInfo;
        ExposeService serviceInfosAnnot = classInfo.getAnnotation(ExposeService.class);
        packageName = classInfo.getPackageInfo().getName();
        className = classInfo.getSimpleName();
        imports.add(classInfo.getImportName());
        classInfo.getParentInterfaces().forEach(it->{
            imports.add(it.getImportName());
            interfaces.add(it.getSimpleName());
        });
        path = serviceInfosAnnot.path();
        domain=serviceInfosAnnot.domain();
        name = serviceInfosAnnot.name();
        version = serviceInfosAnnot.version();
        status = serviceInfosAnnot.status();

        classInfo.getDeclaredMethods().stream()
                .filter(methodInfo -> methodInfo.getAnnotation(ExposeMethod.class) != null)
                .map(method -> {
                    ServiceExpositionMethodDef methodDef = new ServiceExpositionMethodDef(method);
                    imports.addAll(methodDef.getImports());
                    return methodList.add(methodDef);
                })
                .collect(Collectors.toList());


    }

    public String getPath(){
        return path;
    }

    public String getDomain() {
        return domain;
    }

    public String getPackage() {
        return packageName;
    }

    public VersionStatus getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getClassName() {
        return className;
    }

    public String getClientSimpleClassName(){
        return classInfo.getSimpleName()+ REST_CLIENT_SUFFIX;
    }

    public String getServerSimpleClassName(){
        return classInfo.getSimpleName()+ REST_SERVICE_SUFFIX;
    }

    public String getClientClassName(){
        return classInfo.getFullName()+ REST_CLIENT_SUFFIX;
    }

    public String getServerClassName(){
        return classInfo.getFullName()+ REST_SERVICE_SUFFIX;
    }


    public List<String> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    public List<ServiceExpositionMethodDef> getMethods() {
        return Collections.unmodifiableList(methodList);
    }

    public boolean hasGlobalContextFactory(){
        return methodList.stream().filter(ServiceExpositionMethodDef::hasGlobalContextParam).count()>0;
    }

    public boolean hasUserFactory(){
        return methodList.stream().filter(ServiceExpositionMethodDef::hasUserParam).count()>0;
    }


    public Set<String> getImports() {
        return imports;
    }


}

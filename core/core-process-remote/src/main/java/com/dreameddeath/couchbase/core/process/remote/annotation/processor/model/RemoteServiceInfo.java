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

package com.dreameddeath.couchbase.core.process.remote.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
public class RemoteServiceInfo {
    private String path;
    private String domain;
    private String name;
    private String version;
    private String className;
    private String packageName;
    private JobInfo jobInfo;

    public RemoteServiceInfo(ClassInfo classInfo){
        RestExpose restInfoAnnot = classInfo.getAnnotation(RestExpose.class);
        path = restInfoAnnot.rootPath();
        domain = restInfoAnnot.domain();
        name = restInfoAnnot.name();
        version = restInfoAnnot.version();
        className = "Remote"+classInfo.getSimpleName()+"Service";
        packageName = classInfo.getPackageInfo().getName().replaceAll("\\.model\\b",".service.rest");
        jobInfo = new JobInfo(classInfo);

    }

    public String getPath() {
        return path;
    }

    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public String getFullName(){
        return getPackageName()+"."+className;
    }
    public String getVersion() {
        return version;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public JobInfo getJob(){
        return jobInfo;
    }

    public RestModel getRequest(){
        return jobInfo.getRequest();
    }

    public RestModel getResponse(){
        return getJob().getResponse();
    }
}

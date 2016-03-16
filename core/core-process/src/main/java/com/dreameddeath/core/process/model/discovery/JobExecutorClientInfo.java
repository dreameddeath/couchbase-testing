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

package com.dreameddeath.core.process.model.discovery;

import com.dreameddeath.core.curator.model.IRegisterable;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.service.IHasServiceClient;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/03/2016.
 */
public class JobExecutorClientInfo implements IRegisterable {
    @JsonProperty("uuid")
    private UUID uuid;
    @JsonProperty("className")
    private String className;
    @JsonProperty("jobEntity")
    private EntityDef jobEntity;
    @JsonProperty("jobProcessingService")
    private String jobProcessingClassName;
    @JsonProperty("jobProcessingVersion")
    private String jobProcessingVersion;

    @JsonProperty("jobExecutorService")
    private String jobExecutorClassName;
    @JsonProperty("remoteJobServiceClientUid")
    private String remoteJobServiceClientUUID;
    @JsonProperty("remoteJobServiceClientName")
    private String remoteJobServiceClientName;
    @JsonProperty("daemonUid")
    private String daemonUid;
    @JsonProperty("webServerUid")
    private String webServerUid;

    public JobExecutorClientInfo(){}

    public JobExecutorClientInfo(IJobExecutorClient client){
        this.uuid = client.getInstanceUUID();
        this.className = client.getClass().getName();
        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass(client.getJobClass());
        this.jobEntity = EntityDef.build(structureReflection);

        this.jobProcessingClassName = client.getProcessingService().getClass().getName();
        JobProcessingForClass annot = client.getProcessingService().getClass().getAnnotation(JobProcessingForClass.class);
        if(annot!=null){
            this.jobProcessingVersion = annot.version();
        }

        this.jobExecutorClassName = client.getExecutorService().getClass().getName();

        if(client.getExecutorService() instanceof IHasServiceClient){
            this.remoteJobServiceClientUUID = ((IHasServiceClient)client.getExecutorService()).getClientUUID().toString();
            this.remoteJobServiceClientName = ((IHasServiceClient)client.getExecutorService()).getServiceName();
        }
        else if(client.getProcessingService() instanceof IHasServiceClient){
            this.remoteJobServiceClientUUID = ((IHasServiceClient)client.getProcessingService()).getClientUUID().toString();
            this.remoteJobServiceClientName = ((IHasServiceClient)client.getProcessingService()).getServiceName();
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public EntityDef getJobEntity() {
        return jobEntity;
    }

    public void setJobEntity(EntityDef jobEntity) {
        this.jobEntity = jobEntity;
    }

    public String getJobExecutorClassName() {
        return jobExecutorClassName;
    }

    public void setJobExecutorClassName(String jobExecutorClassName) {
        this.jobExecutorClassName = jobExecutorClassName;
    }

    public String getJobProcessingClassName() {
        return jobProcessingClassName;
    }

    public void setJobProcessingClassName(String jobProcessingClassName) {
        this.jobProcessingClassName = jobProcessingClassName;
    }

    public String getJobProcessingVersion() {
        return jobProcessingVersion;
    }

    public void setJobProcessingVersion(String jobProcessingVersion) {
        this.jobProcessingVersion = jobProcessingVersion;
    }

    public String getRemoteJobServiceClientUUID() {
        return remoteJobServiceClientUUID;
    }

    public void setRemoteJobServiceClientUUID(String remoteJobServiceClientUUID) {
        this.remoteJobServiceClientUUID = remoteJobServiceClientUUID;
    }

    public String getRemoteJobServiceClientName() {
        return remoteJobServiceClientName;
    }

    public void setRemoteJobServiceClientName(String remoteJobServiceClientName) {
        this.remoteJobServiceClientName = remoteJobServiceClientName;
    }

    public String getDaemonUid() {
        return daemonUid;
    }

    public void setDaemonUid(String daemonUid) {
        this.daemonUid = daemonUid;
    }

    public String getWebServerUid() {
        return webServerUid;
    }

    public void setWebServerUid(String webServerUid) {
        this.webServerUid = webServerUid;
    }

    @Override @JsonIgnore
    public String getUid() {
        return uuid.toString();
    }
}

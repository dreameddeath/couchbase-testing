/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.process.model.discovery;

import com.dreameddeath.core.curator.model.IRegisterable;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.process.VersionStatus;
import com.dreameddeath.core.process.annotation.ProcessorInfo;
import com.dreameddeath.core.process.service.IHasServiceClient;
import com.dreameddeath.core.process.service.ITaskExecutorClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/03/2016.
 */
public class TaskExecutorClientInfo implements IRegisterable {
    @JsonProperty("clientUuid")
    private UUID clientUuid;
    @JsonProperty("clientClassName")
    private String clientClassName;
    @JsonProperty("entity")
    private EntityDef taskEntity;
    @JsonProperty("processingName")
    private String taskProcessingName=null;
    @JsonProperty("processingDomain")
    private String taskProcessingDomain=null;
    @JsonProperty("processingService")
    private String taskProcessingClassName;
    @JsonProperty("processingVersion")
    private String taskProcessingVersion="1.0.0";
    @JsonProperty("processingVersionState")
    private VersionStatus taskProcessingVersionState=VersionStatus.STABLE;
    @JsonProperty("executorService")
    private String taskExecutorClassName;
    @JsonProperty("remoteServiceClientUid")
    private String remoteTaskServiceClientUUID;
    @JsonProperty("remoteServiceClientName")
    private String remoteTaskServiceClientName;
    @JsonProperty("webServerUid")
    private String webServerUid;
    @JsonProperty("daemonUid")
    private String daemonUid;
    @JsonProperty("domain")
    private String domain;


    public TaskExecutorClientInfo(){}

    public TaskExecutorClientInfo(ITaskExecutorClient executorClient){
        this.clientUuid = executorClient.getInstanceUUID();
        this.domain = executorClient.getDomain();
        this.clientClassName = executorClient.getClass().getName();
        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass(executorClient.getTaskClass());
        this.taskEntity = EntityDef.build(structureReflection);
        this.taskExecutorClassName = executorClient.getExecutorService().getClass().getName();
        
        
        this.taskProcessingClassName = executorClient.getProcessingService().getClass().getName();
        this.taskProcessingClassName = executorClient.getProcessingService().getClass().getName();
        ProcessorInfo annot = executorClient.getProcessingService().getClass().getAnnotation(ProcessorInfo.class);
        if(annot!=null){
            this.taskProcessingName = annot.name();
            this.taskProcessingDomain = annot.domain();
            this.taskProcessingVersion = annot.version();
            this.taskProcessingVersionState = annot.state();
        }
        if(StringUtils.isEmpty(this.taskProcessingName)){
            this.taskProcessingName = taskProcessingClassName.substring(taskProcessingClassName.lastIndexOf(".")+1);
        }
        if(StringUtils.isEmpty(taskProcessingDomain)){
            this.taskProcessingDomain=this.taskEntity.getModelId().getDomain();
        }

        if(executorClient.getExecutorService() instanceof IHasServiceClient){
            this.remoteTaskServiceClientUUID = ((IHasServiceClient)executorClient.getExecutorService()).getClientUUID().toString();
            this.remoteTaskServiceClientName = ((IHasServiceClient)executorClient.getExecutorService()).getServiceName();
        }
        else if(executorClient.getProcessingService() instanceof IHasServiceClient){
            this.remoteTaskServiceClientUUID = ((IHasServiceClient)executorClient.getProcessingService()).getClientUUID().toString();
            this.remoteTaskServiceClientName = ((IHasServiceClient)executorClient.getProcessingService()).getServiceName();
        }
    }

    public UUID getClientUuid() {
        return clientUuid;
    }

    public void setClientUuid(UUID clientUuid) {
        this.clientUuid = clientUuid;
    }

    public String getClientClassName() {
        return clientClassName;
    }

    public void setClientClassName(String clientClassName) {
        this.clientClassName = clientClassName;
    }

    public EntityDef getTaskEntity() {
        return taskEntity;
    }

    public void setTaskEntity(EntityDef taskEntity) {
        this.taskEntity = taskEntity;
    }

    public String getTaskExecutorClassName() {
        return taskExecutorClassName;
    }

    public void setTaskExecutorClassName(String taskExecutorClassName) {
        this.taskExecutorClassName = taskExecutorClassName;
    }

    public String getTaskProcessingClassName() {
        return taskProcessingClassName;
    }

    public void setTaskProcessingClassName(String taskProcessingClassName) {
        this.taskProcessingClassName = taskProcessingClassName;
    }

    public String getTaskProcessingVersion() {
        return taskProcessingVersion;
    }

    public void setTaskProcessingVersion(String taskProcessingVersion) {
        this.taskProcessingVersion = taskProcessingVersion;
    }

    public String getTaskProcessingName() {
        return taskProcessingName;
    }

    public void setTaskProcessingName(String taskProcessingName) {
        this.taskProcessingName = taskProcessingName;
    }

    public String getTaskProcessingDomain() {
        return taskProcessingDomain;
    }

    public void setTaskProcessingDomain(String taskProcessingDomain) {
        this.taskProcessingDomain = taskProcessingDomain;
    }

    public VersionStatus getTaskProcessingVersionState() {
        return taskProcessingVersionState;
    }

    public void setTaskProcessingVersionState(VersionStatus taskProcessingVersionState) {
        this.taskProcessingVersionState = taskProcessingVersionState;
    }

    public String getRemoteTaskServiceClientUUID() {
        return remoteTaskServiceClientUUID;
    }

    public void setRemoteTaskServiceClientUUID(String remoteTaskServiceClientUUID) {
        this.remoteTaskServiceClientUUID = remoteTaskServiceClientUUID;
    }

    public String getRemoteTaskServiceClientName() {
        return remoteTaskServiceClientName;
    }

    public void setRemoteTaskServiceClientName(String remoteTaskServiceClientName) {
        this.remoteTaskServiceClientName = remoteTaskServiceClientName;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override @JsonIgnore
    public String getUid() {
        return clientUuid.toString();
    }
}

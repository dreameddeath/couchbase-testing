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
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.process.VersionStatus;
import com.dreameddeath.core.process.annotation.ProcessorInfo;
import com.dreameddeath.core.process.service.IHasServiceClient;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/03/2016.
 */
public class JobExecutorClientInfo implements IRegisterable {
    @JsonProperty("clientUuid")
    private UUID clientUuid;
    @JsonProperty("clientClassName")
    private String clientClassName;
    @JsonProperty("entity")
    private EntityDef jobEntity;
    @JsonProperty("processingName")
    private String jobProcessingName=null;
    @JsonProperty("processingDomain")
    private String jobProcessingDomain=null;
    @JsonProperty("processingService")
    private String jobProcessingClassName;
    @JsonProperty("processingVersion")
    private String jobProcessingVersion="1.0.0";
    @JsonProperty("processingVersionState")
    private VersionStatus jobProcessingVersionState=VersionStatus.STABLE;
    @JsonProperty("executorService")
    private String jobExecutorClassName;
    @JsonProperty("remoteServiceClientUid")
    private String remoteJobServiceClientUUID;
    @JsonProperty("remoteServiceClientName")
    private String remoteJobServiceClientName;
    @JsonProperty("daemonUid")
    private String daemonUid;
    @JsonProperty("webServerUid")
    private String webServerUid;

    public JobExecutorClientInfo(){}

    public JobExecutorClientInfo(IJobExecutorClient client){
        this.clientUuid = client.getInstanceUUID();
        this.clientClassName = client.getClass().getName();
        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass(client.getJobClass());
        this.jobEntity = EntityDef.build(structureReflection);
        this.jobProcessingClassName = client.getProcessingService().getClass().getName();
        ProcessorInfo annot = client.getProcessingService().getClass().getAnnotation(ProcessorInfo.class);
        if(annot!=null){
            this.jobProcessingName = annot.name();
            this.jobProcessingDomain = annot.domain();
            this.jobProcessingVersion = annot.version();
            this.jobProcessingVersionState = annot.state();
        }
        if(StringUtils.isEmpty(this.jobProcessingName)){
            this.jobProcessingName = jobProcessingClassName.substring(jobProcessingClassName.lastIndexOf(".")+1);
        }
        if(StringUtils.isEmpty(jobProcessingDomain)){
            this.jobProcessingDomain=this.jobEntity.getModelId().getDomain();
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

    public String getJobProcessingName() {
        return jobProcessingName;
    }

    public void setJobProcessingName(String jobProcessingName) {
        this.jobProcessingName = jobProcessingName;
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

    public String getJobProcessingDomain() {
        return jobProcessingDomain;
    }

    public void setJobProcessingDomain(String jobProcessingDomain) {
        this.jobProcessingDomain = jobProcessingDomain;
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

    public VersionStatus getJobProcessingVersionState() {
        return jobProcessingVersionState;
    }

    public void setJobProcessingVersionState(VersionStatus jobProcessingVersionState) {
        this.jobProcessingVersionState = jobProcessingVersionState;
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
        return clientUuid.toString();
    }
}

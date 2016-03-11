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
import com.dreameddeath.core.process.service.IHasServiceClient;
import com.dreameddeath.core.process.service.ITaskExecutorClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/03/2016.
 */
public class TaskExecutorClientInfo implements IRegisterable {
    @JsonProperty("uuid")
    private UUID uuid;
    @JsonProperty("className")
    private String className;
    @JsonProperty("taskClass")
    private String taskClassName;
    @JsonProperty("taskExecutorService")
    private String taskExecutorClassName;
    @JsonProperty("taskProcessingService")
    private String taskProcessingClassName;
    @JsonProperty("remoteTaskServiceClient")
    private String remoteTaskServiceClientUUID;
    @JsonProperty("remoteTaskServiceClientName")
    private String remoteTaskServiceClientName;
    @JsonProperty("daemonUid")
    private String daemonUid;
    @JsonProperty("webServerUid")
    private String webServerUid;


    public TaskExecutorClientInfo(){}

    public TaskExecutorClientInfo(ITaskExecutorClient executorClient){
        this.uuid = executorClient.getInstanceUUID();
        this.className = executorClient.getClass().getName();
        this.taskClassName = executorClient.getTaskClass().getName();
        this.taskExecutorClassName = executorClient.getExecutorService().getClass().getName();
        this.taskProcessingClassName = executorClient.getProcessingService().getClass().getName();
        if(executorClient.getExecutorService() instanceof IHasServiceClient){
            this.remoteTaskServiceClientUUID = ((IHasServiceClient)executorClient.getExecutorService()).getClientUUID().toString();
            this.remoteTaskServiceClientName = ((IHasServiceClient)executorClient.getExecutorService()).getServiceName();
        }
        else if(executorClient.getProcessingService() instanceof IHasServiceClient){
            this.remoteTaskServiceClientUUID = ((IHasServiceClient)executorClient.getProcessingService()).getClientUUID().toString();
            this.remoteTaskServiceClientName = ((IHasServiceClient)executorClient.getProcessingService()).getServiceName();
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

    public String getTaskClassName() {
        return taskClassName;
    }

    public void setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
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

    @Override @JsonIgnore
    public String getUid() {
        return uuid.toString();
    }
}

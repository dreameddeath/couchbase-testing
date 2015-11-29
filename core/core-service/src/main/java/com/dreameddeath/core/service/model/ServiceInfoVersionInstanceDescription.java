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

package com.dreameddeath.core.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 02/10/2015.
 */
public class ServiceInfoVersionInstanceDescription {
    @JsonProperty("uid")
    private String uid;
    @JsonProperty("address")
    private String address;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("uriSpec")
    private String uriSpec;
    @JsonProperty("daemonUid")
    private String daemonUid;
    @JsonProperty("webServerUid")
    private String webServerUid;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUriSpec() {
        return uriSpec;
    }

    public void setUriSpec(String uriSpec) {
        this.uriSpec = uriSpec;
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
}

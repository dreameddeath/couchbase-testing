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

import com.dreameddeath.core.curator.model.IRegisterable;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 17/12/2015.
 */
public abstract class AbstractClientInstanceInfo implements IRegisterable {
    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("uid")
    private String uid;
    @JsonProperty("daemonUid")
    private String daemonUid;
    @JsonProperty("webServerUid")
    private String webServerUid;
    @JsonProperty("creationDate")
    private DateTime creationDate;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractClientInstanceInfo that = (AbstractClientInstanceInfo) o;

        return uid.equals(that.uid);

    }

    @Override
    public final int hashCode() {
        return uid.hashCode();
    }
}

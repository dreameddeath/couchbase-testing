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

package com.dreameddeath.infrastructure.daemon.services.model;

import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 14/08/2015.
 */
public class StatusResponse {
    private String id;
    private IDaemonLifeCycle.Status status;
    private DateTime startDateTime;
    private String hostname;
    private int port;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IDaemonLifeCycle.Status getStatus() {
        return status;
    }

    public void setStatus(IDaemonLifeCycle.Status status) {
        this.status = status;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


}

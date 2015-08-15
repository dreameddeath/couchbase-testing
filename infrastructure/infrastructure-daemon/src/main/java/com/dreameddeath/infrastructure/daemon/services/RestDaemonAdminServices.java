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

package com.dreameddeath.infrastructure.daemon.services;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.services.model.StatusResponse;
import com.dreameddeath.infrastructure.daemon.services.model.StatusUpdateRequest;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.eclipse.jetty.server.ServerConnector;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 14/08/2015.
 */
@Path("/")
@ServiceDef(name="daemon#admin#status",version="1.0",status = VersionStatus.STABLE)
@Api(value = "/", description = "Daemon Administration service")
public class RestDaemonAdminServices extends AbstractExposableService {
    private IDaemonLifeCycle daemonLifeCycle;

    public void setDaemonLifeCycle(IDaemonLifeCycle daemonLifeCycle){
        this.daemonLifeCycle = daemonLifeCycle;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("status")
    @ApiOperation(value = "give the status of the daemon",
            response = StatusResponse.class,
            position = 0)
    public StatusResponse getStatus(){
        StatusResponse result = new StatusResponse();
        result.setStatus(daemonLifeCycle.getDaemon().getStatus());
        ServerConnector currConnector = (ServerConnector) daemonLifeCycle.getDaemon().getWebServer().getConnectors()[0];
        String host;
        int port = currConnector.getPort();
        try{
            host = currConnector.getHost();
        }
        catch(Exception e){
            host = "localhost";
        }
        result.setHostname(host);
        result.setPort(port);
        return result;
    }


    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("status")
    @ApiOperation(value = "set the status of the daemon",
            response = StatusResponse.class,
            position = 0)
    public StatusResponse setStatus(StatusUpdateRequest statusUpdateRequest){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                    if (statusUpdateRequest.getStatus() == StatusUpdateRequest.Status.START) {
                        daemonLifeCycle.start();
                    }
                    else if(statusUpdateRequest.getStatus() == StatusUpdateRequest.Status.STOP) {
                        daemonLifeCycle.stop();
                    }
                    else{
                        //TODO halt management
                    }
                }
                catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        }).start();

        return getStatus();
    }
}

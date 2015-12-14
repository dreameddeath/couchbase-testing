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

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.model.DaemonInfo;
import com.dreameddeath.infrastructure.daemon.model.DaemonMetricsInfo;
import com.dreameddeath.infrastructure.daemon.services.model.daemon.StatusResponse;
import com.dreameddeath.infrastructure.daemon.services.model.daemon.StatusUpdateRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 14/08/2015.
 */
@Path("/daemon")
@ServiceDef(domain=RestLocalDaemonAdminService.DAEMON_SERVICE_DOMAIN,name=RestLocalDaemonAdminService.DAEMON_SERVICE_NAME,version= RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION,status = VersionStatus.STABLE)
@Api(value = "/daemon", description = "Daemon Administration service")
public class RestLocalDaemonAdminService extends AbstractExposableService {
    private static final Logger LOG = LoggerFactory.getLogger(RestLocalDaemonAdminService.class);
    public static final String DAEMON_SERVICE_DOMAIN ="admin";
    public static final String DAEMON_SERVICE_NAME ="daemon#admin";
    public static final String DAEMON_SERVICE_VERSION ="1.0";

    private final RestLocalConfigAdminService configAdminService=new RestLocalConfigAdminService();
    private RestLocalWebServerAdminService webServerAdminResource;
    private AbstractDaemon daemon;

    @Autowired
    public void setDaemon(AbstractDaemon daemon){
        this.daemon = daemon;
    }

    @Required
    public void setWebServerAdminResource(RestLocalWebServerAdminService resource){
        this.webServerAdminResource = resource;
    }

    @Override
    public String getId(){
        return daemon.getUuid().toString();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "give the status of the daemon",
            response = DaemonInfo.class,
            position = 0)
    public DaemonInfo getInfo(){
        return new DaemonInfo(daemon);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("status")
    @ApiOperation(value = "give the status of the daemon",
            response = StatusResponse.class,
            position = 1)
    public StatusResponse getStatus(){
        return buildStatus(daemon.getStatus());
    }


    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("status")
    @ApiOperation(value = "set the status of the daemon",
            response = StatusResponse.class,
            position = 2)
    public StatusResponse setStatus(StatusUpdateRequest statusUpdateRequest){
        try{
            if (statusUpdateRequest.getAction() == StatusUpdateRequest.Action.START) {
                LOG.info("Starting {}/{}",daemon.getName(),daemon.getUuid());
                daemon.getDaemonLifeCycle().start();
            }
            else if(statusUpdateRequest.getAction() == StatusUpdateRequest.Action.STOP) {
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        LOG.info("Stopping {}/{}", daemon.getName(), daemon.getUuid());
                        daemon.getDaemonLifeCycle().stop();
                    }
                    catch(Exception e){

                    }
                }).start();
                return buildStatus(IDaemonLifeCycle.Status.STOPPING);
            }
            else{
                LOG.info("Halting {}/{}",daemon.getName(),daemon.getUuid());
                daemon.getDaemonLifeCycle().halt();
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }

        return getStatus();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("{name}/metrics")
    @ApiOperation(value = "give metrics on a given daemon",
            response = MetricRegistry.class,
    position = 3)
    public DaemonMetricsInfo getMetrics(){
        return daemon.getDaemonMetrics().getMetrics();
    }

    @Path("webservers")
    public RestLocalWebServerAdminService getWebServerItemResource(){
        return webServerAdminResource;
    }

    @Path("config")
    public RestLocalConfigAdminService getConfigWebService(){
        return configAdminService;
    }

    protected StatusResponse buildStatus(IDaemonLifeCycle.Status status){
        StatusResponse result = new StatusResponse();
        result.setStatus(status);
        return result;
    }


}

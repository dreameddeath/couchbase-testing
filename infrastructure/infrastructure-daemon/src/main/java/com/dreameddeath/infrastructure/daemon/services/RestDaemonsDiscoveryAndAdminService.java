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

import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.utils.ServiceJacksonObjectMapper;
import com.dreameddeath.infrastructure.daemon.discovery.IDaemonDiscovery;
import com.dreameddeath.infrastructure.daemon.model.DaemonInfo;
import com.dreameddeath.infrastructure.daemon.model.WebServerInfo;
import com.dreameddeath.infrastructure.daemon.services.model.daemon.StatusResponse;
import com.dreameddeath.infrastructure.daemon.services.model.daemon.StatusUpdateRequest;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 03/10/2015.
 */
@Path("/daemons")
@Api(value = "/daemons", description = "Daemons Discovery And Administration service")
public class RestDaemonsDiscoveryAndAdminService {
    private IDaemonDiscovery _daemonDiscovery;
    private ServiceClientFactory _serviceFactory;

    public void setDaemonDiscovery(IDaemonDiscovery daemonDiscovery){
        _daemonDiscovery = daemonDiscovery;
    }

    public void setClientFactory(ServiceClientFactory factory){
        _serviceFactory = factory;
    }

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<DaemonInfo> getDaemons() throws Exception{
        return _daemonDiscovery.registeredDaemonInfoList();
    }

    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public DaemonInfo getDaemon(@PathParam("id") String uid) throws Exception{
        return _serviceFactory
                .getClient(RestLocalDaemonAdminService.DAEMON_SERVICE_NAME, RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION,uid)
                .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                .request(MediaType.APPLICATION_JSON)
                .get(DaemonInfo.class);
    }

    @GET
    @Path("/{id}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    public StatusResponse getDaemonStatus(@PathParam("id") String uid) throws Exception{
        return _serviceFactory
                .getClient(RestLocalDaemonAdminService.DAEMON_SERVICE_NAME, RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION,uid)
                .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                .path("/status")
                .request(MediaType.APPLICATION_JSON)
                .get(StatusResponse.class);
    }

    @PUT
    @Path("/{id}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public StatusResponse updateDaemonStatus(@PathParam("id") String uid,StatusUpdateRequest statusUpdateRequest) throws Exception{
        return _serviceFactory
                .getClient(RestLocalDaemonAdminService.DAEMON_SERVICE_NAME, RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION, uid)
                .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                .path("/status")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(statusUpdateRequest), StatusResponse.class);
    }

    @GET
    @Path("/{id}/webservers")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<WebServerInfo> getWebservers(@PathParam("id") String uid){
        return _serviceFactory
                .getClient(RestLocalDaemonAdminService.DAEMON_SERVICE_NAME, RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION, uid)
                .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                .path("/webservers")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<WebServerInfo>>() {
                });
    }

    @GET
    @Path("/{id}/webservers/{wid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public WebServerInfo getWebserverInfo(@PathParam("id") String uid,@PathParam("wid") String webServerId){
        return _serviceFactory
                .getClient(RestLocalDaemonAdminService.DAEMON_SERVICE_NAME, RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION, uid)
                .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                .path("/webservers/{wid}")
                .resolveTemplate("wid", webServerId)
                .request(MediaType.APPLICATION_JSON)
                .get(WebServerInfo.class);
    }

    @GET
    @Path("/{id}/webservers/{wid}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    public com.dreameddeath.infrastructure.daemon.services.model.webserver.StatusResponse getWebserverStatus(@PathParam("id") String uid,@PathParam("wid") String webServerId){
        return _serviceFactory
                .getClient(RestLocalDaemonAdminService.DAEMON_SERVICE_NAME, RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION, uid)
                .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                .path("/webservers/{wid}/status")
                .resolveTemplate("wid", webServerId)
                .request(MediaType.APPLICATION_JSON)
                .get(com.dreameddeath.infrastructure.daemon.services.model.webserver.StatusResponse.class);
    }

    @PUT
    @Path("/{id}/webservers/{wid}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public com.dreameddeath.infrastructure.daemon.services.model.webserver.StatusResponse updateWebserverStatus(@PathParam("id") String uid,@PathParam("wid") String webServerId,com.dreameddeath.infrastructure.daemon.services.model.webserver.StatusUpdateRequest updateRequest){
        return _serviceFactory
                .getClient(RestLocalDaemonAdminService.DAEMON_SERVICE_NAME, RestLocalDaemonAdminService.DAEMON_SERVICE_VERSION, uid)
                .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                .path("/webservers/{wid}/status")
                .resolveTemplate("wid", webServerId)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateRequest), com.dreameddeath.infrastructure.daemon.services.model.webserver.StatusResponse.class);
    }
}

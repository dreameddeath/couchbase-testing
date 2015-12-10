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


import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.infrastructure.daemon.discovery.DaemonDiscovery;
import com.dreameddeath.infrastructure.daemon.model.DaemonInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 03/10/2015.
 */
@Path("/daemons")
@Api(value = "/daemons", description = "Daemons Discovery And Administration service")
public class RestDaemonsDiscoveryAndAdminService {
    @Autowired(required = true)
    private DaemonDiscovery daemonDiscovery;
    private IServiceClient daemonAdminClient;

    public void setDaemonDiscovery(DaemonDiscovery daemonDiscovery){
        this.daemonDiscovery = daemonDiscovery;
    }

    @Required
    public void setDaemonAdminClient(IServiceClient factory){
        daemonAdminClient = factory;
    }

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<DaemonInfo> getDaemons() throws Exception{
        return daemonDiscovery.getList();
    }

    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getDaemon(@PathParam("id") String uid) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }


    @GET
    @Path("/{id}/metrics")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getDaemonMetrics(@PathParam("id") String uid, @PathParam("wid") String webServerId) {
        return daemonAdminClient.getInstance(uid)
                .path("/metrics")
                .request(MediaType.APPLICATION_JSON)
                .get();
    }


    @GET
    @Path("/{id}/config")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getDaemonConfig(@PathParam("id") String uid) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/config")
                .request(MediaType.APPLICATION_JSON)
                .get();
    }


    @GET
    @Path("/{id}/config/{domain}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getDaemonConfigDomain(@PathParam("id") String uid,@PathParam("domain")String domain) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/config/{domain}")
                .resolveTemplate("domain",domain)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    @PUT
    @Path("/{id}/config/{domain}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response updateDaemonConfigDomain(@PathParam("id") String uid,@PathParam("domain")String domain,Map<String,String> updateRequest) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/config/{domain}")
                .resolveTemplate("domain",domain)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(updateRequest,MediaType.APPLICATION_JSON));
    }


    @GET
    @Path("/{id}/config/{domain}/{key}")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response getDaemonConfigDomain(@PathParam("id") String uid,@PathParam("domain")String domain,@PathParam("key")String key) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/config/{domain}/{key}")
                .resolveTemplate("domain",domain)
                .resolveTemplate("key",key)
                .request(MediaType.TEXT_PLAIN)
                .get();
    }

    @POST
    @Path("/{id}/config/{domain}/{key}")
    @Produces({ MediaType.TEXT_PLAIN })
    @Consumes({ MediaType.TEXT_PLAIN })
    public Response addDaemonConfigDomain(@PathParam("id") String uid,@PathParam("domain")String domain,@PathParam("key")String key,String value) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/config/{domain}/{key}")
                .resolveTemplate("domain",domain)
                .resolveTemplate("key",key)
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.text(value));
    }

    @PUT
    @Path("/{id}/config/{domain}/{key}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.TEXT_PLAIN })
    public Response updateDaemonConfigDomain(@PathParam("id") String uid,@PathParam("domain")String domain,@PathParam("key")String key,String value) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/config/{domain}/{key}")
                .resolveTemplate("domain",domain)
                .resolveTemplate("key",key)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.text(value));
    }



    @DELETE
    @Path("/{id}/config/{domain}/{key}")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response deleteDaemonConfigDomain(@PathParam("id") String uid,@PathParam("domain")String domain,@PathParam("key")String key) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/config/{domain}/{key}")
                .resolveTemplate("domain",domain)
                .resolveTemplate("key",key)
                .request(MediaType.TEXT_PLAIN)
                .delete();
    }



    @GET
    @Path("/{id}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getDaemonStatus(@PathParam("id") String uid) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/status")
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    @PUT
    @Path("/{id}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response updateDaemonStatus(@PathParam("id") String uid,InputStream statusUpdateRequest) throws Exception{
        return daemonAdminClient.getInstance(uid)
                .path("/status")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(statusUpdateRequest));
    }

    @GET
    @Path("/{id}/webservers")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getWebservers(@PathParam("id") String uid){
        return daemonAdminClient.getInstance(uid)
                .path("/webservers")
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    @GET
    @Path("/{id}/webservers/{wid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getWebserverInfo(@PathParam("id") String uid,@PathParam("wid") String webServerId){
        return daemonAdminClient.getInstance(uid)
                .path("/webservers/{wid}")
                .resolveTemplate("wid", webServerId)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    @GET
    @Path("/{id}/webservers/{wid}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getWebserverStatus(@PathParam("id") String uid,@PathParam("wid") String webServerId){
        return daemonAdminClient.getInstance(uid)
                .path("/webservers/{wid}/status")
                .resolveTemplate("wid", webServerId)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    @GET
    @Path("/{id}/webservers/{wid}/metrics")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getWebserverMetrics(@PathParam("id") String uid, @PathParam("wid") String webServerId) {
        return daemonAdminClient.getInstance(uid)
                .path("/webservers/{wid}/metrics")
                .resolveTemplate("wid", webServerId)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }


    @PUT
    @Path("/{id}/webservers/{wid}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response updateWebserverStatus(@PathParam("id") String uid,@PathParam("wid") String webServerId,InputStream updateRequest){
        return daemonAdminClient.getInstance(uid)
                .path("/webservers/{wid}/status")
                .resolveTemplate("wid", webServerId)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateRequest));
    }
}

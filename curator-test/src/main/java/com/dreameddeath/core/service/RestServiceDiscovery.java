package com.dreameddeath.core.service;

import com.dreameddeath.core.model.ServicesInstanceDescription;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by CEAJ8230 on 18/01/2015.
 */
@Path("/apis")
public class RestServiceDiscovery {
    private ServiceDiscoverer _serviceDiscoverer;

    public void setServiceDiscoverer(ServiceDiscoverer serviceDiscoverer){
        _serviceDiscoverer = serviceDiscoverer;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ServicesInstanceDescription getServices() throws Exception{
        return _serviceDiscoverer.getInstancesDescription();
    }
}

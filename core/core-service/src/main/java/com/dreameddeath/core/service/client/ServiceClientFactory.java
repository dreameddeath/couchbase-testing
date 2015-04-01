package com.dreameddeath.core.service.client;

import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

//import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


/**
 * Created by CEAJ8230 on 04/03/2015.
 */
public class ServiceClientFactory {
    private final ServiceDiscoverer _serviceDiscoverer;
    private ConcurrentMap<String,WebTarget> _clientPerUri = new ConcurrentHashMap<>();

    public ServiceClientFactory(ServiceDiscoverer serviceDiscoverer){
        _serviceDiscoverer = serviceDiscoverer;
    }

    public WebTarget getClient(String serviceName,String serviceVersion){
        try{
            String uri = _serviceDiscoverer.getInstance(ServiceNamingUtils.buildServiceFullName(serviceName,serviceVersion)).buildUriSpec();
            return _clientPerUri.computeIfAbsent(uri, new Function<String, WebTarget>() {
                @Override
                public WebTarget apply(String s) {
                    return ClientBuilder.newBuilder().build().target(UriBuilder.fromUri(uri));
                }
            })
            ;
        }
        catch(ServiceDiscoveryException e){
            //Todo throw an error
        }
        return null;
    }

}

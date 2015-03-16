package com.dreameddeath.core.service.model;

import java.util.*;

/**
 * Created by CEAJ8230 on 18/01/2015.
 */
public class ServicesInstanceDescription {
    private Map<String,List<ServiceInstanceDescription>> _serviceInstanceMap =new HashMap<>();

    public void setMap(Map<String,List<ServiceInstanceDescription>> map){
        _serviceInstanceMap.clear();
        _serviceInstanceMap.putAll(map);
    }

    public  Map<String,List<ServiceInstanceDescription>> getServiceInstanceMap(){
        return Collections.unmodifiableMap(_serviceInstanceMap);
    }

    public void addServiceInstance(ServiceInstanceDescription serviceDescr){
        if(!_serviceInstanceMap.containsKey(serviceDescr.getName())){
            _serviceInstanceMap.put(serviceDescr.getName(),new ArrayList<>());
        }
        _serviceInstanceMap.get(serviceDescr.getName()).add(serviceDescr);
    }

}

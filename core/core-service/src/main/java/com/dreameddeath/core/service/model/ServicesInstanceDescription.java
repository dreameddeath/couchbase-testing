/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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

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

package com.dreameddeath.core.service;

import com.dreameddeath.core.service.registrar.IEndPointDescription;
import com.dreameddeath.core.service.registrar.RestServiceRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by Christophe Jeunesse on 15/01/2015.
 */
public abstract class AbstractExposableService {
    private String address="";
    private IEndPointDescription endPoint;
    private RestServiceRegistrar serviceRegistrar;


    public String getId(){
        return endPoint.buildInstanceUid();
    }

    @Required
    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddress() {
        return address;
    }

    @Autowired
    public void setEndPoint(IEndPointDescription obj){
        endPoint = obj;
    }
    public IEndPointDescription getEndPoint() {
        return endPoint;
    }

    @Autowired
    public void setServiceRegistrar(RestServiceRegistrar serviceRegistrar){
        this.serviceRegistrar = serviceRegistrar;
        serviceRegistrar.addService(this);
    }

    public RestServiceRegistrar getServiceRegistrar() {
        return serviceRegistrar;
    }
}

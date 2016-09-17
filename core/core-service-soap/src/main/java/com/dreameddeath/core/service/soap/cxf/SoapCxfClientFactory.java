/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.service.soap.cxf;

import com.dreameddeath.core.service.client.AbstractServiceClientFactory;
import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.soap.ISoapClient;
import com.dreameddeath.core.service.soap.handler.SoapHandlerFactory;
import com.dreameddeath.core.service.soap.model.SoapCuratorDiscoveryServiceDescription;
import com.google.common.collect.ImmutableList;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.cxf.Bus;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.ws.handler.Handler;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/09/2016.
 */
public class SoapCxfClientFactory<TSERV> extends AbstractServiceClientFactory<ISoapClient<TSERV>,SoapCuratorDiscoveryServiceDescription> {
    private Bus bus;
    private SoapHandlerFactory handlerFactory=null;

    @Autowired
    public void setBus(Bus bus){
        this.bus = bus;
    }

    @Autowired(required = false)
    public void setHandlerFactory(SoapHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    public SoapCxfClientFactory(AbstractServiceDiscoverer serviceDiscoverer) {
        super(serviceDiscoverer);
    }

    public SoapCxfClientFactory(AbstractServiceDiscoverer serviceDiscoverer, ClientRegistrar registrar) {
        super(serviceDiscoverer, registrar);
    }

    @Override
    protected ISoapClient<TSERV> buildClient(ServiceProvider<SoapCuratorDiscoveryServiceDescription> provider, String serviceFullName) {
        return new SoapCxfClient<>(provider,serviceFullName,this);
    }

    public Bus getBus() {
        return bus;
    }

    public List<Handler> getHandlers(){
        if(handlerFactory==null) {
            return ImmutableList.of();
        }
        return handlerFactory.getHandlerList(true);
    }
}

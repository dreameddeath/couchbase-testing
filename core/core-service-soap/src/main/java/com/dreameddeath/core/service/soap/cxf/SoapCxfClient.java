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

import com.dreameddeath.core.service.client.AbstractServiceClientImpl;
import com.dreameddeath.core.service.soap.ISoapClient;
import com.dreameddeath.core.service.soap.model.SoapCuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.utils.UriUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * Created by Christophe Jeunesse on 07/09/2016.
 */
public class SoapCxfClient<T> extends AbstractServiceClientImpl<T,SoapCuratorDiscoveryServiceDescription> implements ISoapClient<T> {
    private final static Logger LOG = LoggerFactory.getLogger(SoapCxfClient.class);
    private final SoapCxfClientFactory<T> soapParentFactory;
    private final Cache<ServiceInstance<SoapCuratorDiscoveryServiceDescription>,ClientWrapper<T>> cacheInstance;

    public SoapCxfClient(ServiceProvider<SoapCuratorDiscoveryServiceDescription> provider, String serviceFullName, SoapCxfClientFactory<T> soapFactory){
        super(provider,serviceFullName,soapFactory);
        this.soapParentFactory =soapFactory;
        this.cacheInstance =
                CacheBuilder.newBuilder()
                        .weakKeys()
                        .removalListener(reason-> LOG.info("Removing client % due to %s",reason.getValue(),reason.getCause()))
                        .build();
    }

    @Override
    protected T buildClient(final ServiceInstance<SoapCuratorDiscoveryServiceDescription> instance) {
        try {
            return cacheInstance.get(instance, () -> {
                JaxWsProxyFactoryBean clientFactoryBean=new JaxWsProxyFactoryBean();
                String fullAddress = UriUtils.buildUri(instance,true);
                clientFactoryBean.setServiceClass(Thread.currentThread().getContextClassLoader().loadClass(instance.getPayload().getClassName()));
                clientFactoryBean.setBus(soapParentFactory.getBus());
                clientFactoryBean.getHandlers().addAll(soapParentFactory.getHandlers());
                clientFactoryBean.setAddress(fullAddress);
                return ClientWrapper.<T>build(clientFactoryBean,instance);
            }).get();
        }
        catch(ExecutionException e){
            LOG.error("Error during generation of client for instance {}",instance);
            LOG.error("The exception is",e);
            throw new RuntimeException(e);
        }
    }

    private static class ClientWrapper<T>{
        private final T client;
        private final String uid;
        private final String serviceName;
        private final String serviceVersion;
        private final String address;
        private final String className;

        public ClientWrapper(JaxWsProxyFactoryBean factoryBean,ServiceInstance<SoapCuratorDiscoveryServiceDescription> instance) {
            this.uid = instance.getId();
            this.serviceName = instance.getPayload().getName();
            this.serviceVersion = instance.getPayload().getVersion();
            this.address = factoryBean.getAddress();
            this.className = factoryBean.getServiceClass().getName();
            this.client = (T)factoryBean.create();
        }

        public static <T> ClientWrapper<T> build(JaxWsProxyFactoryBean factory,ServiceInstance<SoapCuratorDiscoveryServiceDescription> instance){
            return new ClientWrapper<>(factory,instance);
        }

        public T get(){
            return client;
        }

        @Override
        public String toString(){
            return "{" +
                    "uid=" + uid +
                    ",serviceName=" + serviceName +
                    ",version=" + serviceVersion +
                    ",address=" + address +
                    ",className=" + className +
                    "}";
        }
    }
}

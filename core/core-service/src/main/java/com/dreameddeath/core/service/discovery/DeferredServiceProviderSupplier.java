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

package com.dreameddeath.core.service.discovery;

import org.apache.curator.x.discovery.ServiceProvider;

import java.util.function.Supplier;

/**
 * Created by Christophe Jeunesse on 25/10/2016.
 */
public class DeferredServiceProviderSupplier<T> implements IServiceProviderSupplier<T>{
    private final Supplier<ServiceProvider<T>> initMethod;
    private volatile ServiceProvider<T> serviceProvider=null;

    public DeferredServiceProviderSupplier(Supplier<ServiceProvider<T>> init){
        this.initMethod=init;
    }

    @Override
    public ServiceProvider<T> getServiceProvider(){
        if(serviceProvider==null){
            synchronized (this){
                if(serviceProvider==null){
                    serviceProvider = initMethod.get();
                }
            }
        }
        return serviceProvider;
    }
}

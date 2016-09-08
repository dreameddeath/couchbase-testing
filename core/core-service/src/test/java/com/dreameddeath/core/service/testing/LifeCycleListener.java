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

package com.dreameddeath.core.service.testing;

import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.registrar.AbstractServiceRegistrar;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 14/01/2015.
 */
public class LifeCycleListener implements LifeCycle.Listener {
    private static Logger LOG = LoggerFactory.getLogger(LifeCycleListener.class);
    private final AbstractServiceRegistrar serviceRegistrar;
    private final AbstractServiceDiscoverer serviceDiscoverer;

    public LifeCycleListener(AbstractServiceRegistrar serviceRegistrar){
        this(serviceRegistrar,null);
    }


    public LifeCycleListener(AbstractServiceDiscoverer serviceDiscoverer){
        this(null,serviceDiscoverer);
    }

    public LifeCycleListener(AbstractServiceRegistrar serviceRegistrar, AbstractServiceDiscoverer serviceDiscoverer){
        this.serviceRegistrar=serviceRegistrar;
        this.serviceDiscoverer=serviceDiscoverer;
    }


    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {
        try {
            if(serviceRegistrar!=null) {
                serviceRegistrar.start();
            }
        }catch(Throwable e){
            LOG.error("Error",e);
        }
        try{
            if(serviceDiscoverer!=null){
                serviceDiscoverer.start();
            }
        }
        catch (Throwable e){
            LOG.error("Error",e);

        }
    }

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {

    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle) {
        try{
            serviceRegistrar.stop();
        }
        catch (Throwable e){
            LOG.error("Error",e);
        }
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {

    }
}

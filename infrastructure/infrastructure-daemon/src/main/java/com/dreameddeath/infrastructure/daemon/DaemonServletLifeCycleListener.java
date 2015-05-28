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

package com.dreameddeath.infrastructure.daemon;

import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 20/05/2015.
 */
public class DaemonServletLifeCycleListener implements LifeCycle.Listener {
    private static Logger LOG = LoggerFactory.getLogger(DaemonServletLifeCycleListener.class);
    private final ServiceRegistrar _serviceRegistrar;
    private final ServiceDiscoverer _serviceDiscoverer;

    public DaemonServletLifeCycleListener(ServiceRegistrar serviceRegistrar){
        this(serviceRegistrar,null);
    }


    public DaemonServletLifeCycleListener(ServiceDiscoverer serviceDiscoverer){
        this(null,serviceDiscoverer);
    }

    public DaemonServletLifeCycleListener(ServiceRegistrar serviceRegistrar, ServiceDiscoverer serviceDiscoverer){
        _serviceRegistrar=serviceRegistrar;
        _serviceDiscoverer=serviceDiscoverer;
    }


    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {
        try {
            if(_serviceRegistrar!=null) {
                _serviceRegistrar.start();
            }
        }catch(Throwable e){
            LOG.error("Error",e);
        }
        try{
            if(_serviceDiscoverer!=null){
                _serviceDiscoverer.start();
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
            _serviceRegistrar.stop();
        }
        catch (Throwable e){
            LOG.error("Error",e);
        }
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {

    }
}

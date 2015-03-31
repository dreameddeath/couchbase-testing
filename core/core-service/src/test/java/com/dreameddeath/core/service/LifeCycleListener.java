package com.dreameddeath.core.service;

import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by CEAJ8230 on 14/01/2015.
 */
public class LifeCycleListener implements LifeCycle.Listener {
    private static Logger LOG = LoggerFactory.getLogger(LifeCycleListener.class);
    private final ServiceRegistrar _serviceRegistrar;
    private final ServiceDiscoverer _serviceDiscoverer;
    public LifeCycleListener(ServiceRegistrar serviceRegistrar){
        this(serviceRegistrar,null);
        //_serviceRegistrar=serviceRegistrar;
    }


    public LifeCycleListener(ServiceDiscoverer serviceDiscoverer){
        this(null,serviceDiscoverer);
    }

    public LifeCycleListener(ServiceRegistrar serviceRegistrar,ServiceDiscoverer serviceDiscoverer){
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
        try {
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

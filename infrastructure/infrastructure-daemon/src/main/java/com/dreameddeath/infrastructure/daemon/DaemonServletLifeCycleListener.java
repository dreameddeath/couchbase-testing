package com.dreameddeath.infrastructure.daemon;

import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by CEAJ8230 on 20/05/2015.
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

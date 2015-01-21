package com.dreameddeath.curator.server;

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
    public LifeCycleListener(ServiceRegistrar serviceRegistrar){
        _serviceRegistrar=serviceRegistrar;
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {
        try {
            _serviceRegistrar.start();
        }catch(Throwable e){
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

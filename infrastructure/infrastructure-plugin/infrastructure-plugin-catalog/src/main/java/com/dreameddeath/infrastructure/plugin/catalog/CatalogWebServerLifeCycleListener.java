package com.dreameddeath.infrastructure.plugin.catalog;

import com.dreameddeath.core.dao.exception.DaoException;
import org.eclipse.jetty.util.component.LifeCycle;

public class CatalogWebServerLifeCycleListener implements LifeCycle.Listener  {
    private final CatalogWebServerPlugin parent;

    public CatalogWebServerLifeCycleListener(CatalogWebServerPlugin parent) {
        this.parent = parent;
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle) {
        try {
            this.parent.getCatalogServiceFactory().init();
        }
        catch (DaoException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {

    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {
        this.parent.getCatalogServiceFactory().cleanup();
    }
}

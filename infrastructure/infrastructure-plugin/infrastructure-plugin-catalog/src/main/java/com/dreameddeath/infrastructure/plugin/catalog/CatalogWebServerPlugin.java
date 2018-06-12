package com.dreameddeath.infrastructure.plugin.catalog;

import com.dreameddeath.couchbase.core.catalog.service.CatalogServiceFactory;
import com.dreameddeath.infrastructure.daemon.plugin.AbstractWebServerPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.IWebServerPluginBuilder;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class CatalogWebServerPlugin extends AbstractWebServerPlugin {
    private static final String GLOBAL_CATALOG_FACTORY_PARAM_NAME = "catalogFactory";
    private final CatalogServiceFactory factory;

    private CatalogWebServerPlugin(Builder builder, AbstractWebServer<?> parent) {
        super(parent);
        CouchbaseWebServerPlugin plugin = parent.getPlugin(CouchbaseWebServerPlugin.class);
        factory = new CatalogServiceFactory();
        factory.setFactory(plugin.getDocumentDaoFactory());
        factory.setDateTimeService(parent.getDateTimeServiceFactory().getService());
        factory.setMetricRegistry(parent.getMetricRegistry());

        parent.getLifeCycle().addLifeCycleListener(new CatalogWebServerLifeCycleListener(this));
    }

    @Override
    public void enrich(ServletContextHandler handler) {
        super.enrich(handler);
        handler.setAttribute(GLOBAL_CATALOG_FACTORY_PARAM_NAME,factory);
    }

    public CatalogServiceFactory getCatalogServiceFactory() {
        return factory;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder implements IWebServerPluginBuilder<CatalogWebServerPlugin> {

        @Override
        public CatalogWebServerPlugin build(AbstractWebServer parent) {
            return new CatalogWebServerPlugin(this,parent);
        }
    }
}

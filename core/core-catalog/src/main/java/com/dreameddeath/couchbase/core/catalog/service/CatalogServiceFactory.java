package com.dreameddeath.couchbase.core.catalog.service;

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.dreameddeath.couchbase.core.catalog.service.impl.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CatalogServiceFactory {
    private final Map<String,CatalogService> catalogServiceMap = new ConcurrentHashMap<>();
    private final boolean selfRegisterDaoDomain;
    private CouchbaseDocumentDaoFactory factory;
    private IDateTimeService dateTimeService;
    private MetricRegistry metricRegistry;
    private boolean initialized = false;

    @Autowired
    public void setFactory(CouchbaseDocumentDaoFactory factory) {
        this.factory = factory;
    }

    @Autowired
    public void setDateTimeService(IDateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Autowired
    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public CatalogServiceFactory(){
        this(true);
    }

    public CatalogServiceFactory(boolean selfRegisterDaoDomain) {
        this.selfRegisterDaoDomain = selfRegisterDaoDomain;
    }

    @PostConstruct
    public void init() throws DaoException {
        for(CatalogService service:catalogServiceMap.values()){
            service.init();
        }
        this.initialized = true;
    }

    public CatalogService getCatalogService(String domain){
        return getCatalogService(domain,null);
    }

    public CatalogService getCatalogService(String domain,String keyPrefix){
        return catalogServiceMap.computeIfAbsent(domain,domainName->{
            if(selfRegisterDaoDomain){
                try {
                    factory.addDaoForGivenDomainEntity(Catalog.class, domain);
                }
                catch(Throwable e){
                    throw new RuntimeException("Cannot add catalog dao for domain "+domain,e);
                }
            }
            CatalogService service = new CatalogService(domainName, keyPrefix, factory, dateTimeService, metricRegistry);
            if(this.initialized){
                try {
                    service.init();
                }
                catch (DaoException e){
                    throw new RuntimeException(e);
                }
            }
            return service;
        });
    }

    public void cleanup() {
        for(CatalogService catalogService : this.catalogServiceMap.values()){
            catalogService.cleanup();
        }
        this.catalogServiceMap.clear();
    }
}

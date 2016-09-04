package com.dreameddeath.couchbase.testing.daemon;

import com.dreameddeath.core.service.client.AbstractServiceClientFactory;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.discovery.rest.RestServiceDiscoverer;
import com.dreameddeath.core.service.utils.RestServiceTypeHelper;
import com.dreameddeath.core.service.utils.ServiceTypeUtils;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Christophe Jeunesse on 04/05/2016.
 */
public class DaemonWrapperForTesting {
    private final AbstractDaemon daemon;
    private Thread startingThread;

    public DaemonWrapperForTesting(AbstractDaemon  daemon){
        this.daemon = daemon;
    }

    public void start() throws Exception{
        CountDownLatch isStarted = new CountDownLatch(1);
        final AtomicReference<Exception> startingThreadException=new AtomicReference<>(null);
        startingThread = new Thread(() -> {
            try {
                daemon.getDaemonLifeCycle().start();
                isStarted.countDown();
                daemon.getDaemonLifeCycle().join();
            }
            catch (Exception e){
                startingThreadException.set(e);
                isStarted.countDown();
            }
        });
        startingThread.start();
        isStarted.await(1, TimeUnit.MINUTES);
        if(startingThreadException.get()!=null){
            throw new RuntimeException("Starting thread daemon issue",startingThreadException.get());
        }
    }


    public AbstractDaemon getDaemon() {
        return daemon;
    }

    public void stop()throws Exception{
        if(daemon!=null){
            daemon.getDaemonLifeCycle().stop();
        }
        if(startingThread!=null && startingThread.isAlive()){
            startingThread.join(60*1000,0);
        }
    }

    public AbstractServiceDiscoverer getServiceDiscoveryForDomain(String domain,String serviceType){
        AbstractServiceDiscoverer discoverer = ServiceTypeUtils.getDefinition(serviceType).buildDiscoverer(daemon.getCuratorClient(),domain);
        try {
            discoverer.start();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return discoverer;
    }

    public RestServiceDiscoverer getServiceDiscoveryForDomain(String domain){
        return (RestServiceDiscoverer) getServiceDiscoveryForDomain(domain, RestServiceTypeHelper.SERVICE_TYPE);
    }

    public AbstractServiceClientFactory getServiceFactoryForDomain(String domain,String serviceType){
        return ServiceTypeUtils.getDefinition(serviceType).buildClientFactory(getServiceDiscoveryForDomain(domain,serviceType));
    }

    public RestServiceClientFactory getServiceFactoryForDomain(String domain){
        return (RestServiceClientFactory)getServiceFactoryForDomain(domain,RestServiceTypeHelper.SERVICE_TYPE);
    }
}

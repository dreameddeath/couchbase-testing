package com.dreameddeath.core.notification.discoverer;

import com.dreameddeath.core.curator.discovery.impl.CuratorDiscoveryImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class ListenerDiscoverer extends CuratorDiscoveryImpl<ListenerDescription>{
    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    public ListenerDiscoverer(CuratorFramework curatorFramework, String basePath) {
        super(curatorFramework, basePath);
    }

    @Override
    protected ListenerDescription deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, ListenerDescription.class);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}

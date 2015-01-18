package com.dreameddeath.curator.server;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by CEAJ8230 on 13/01/2015.
 */
public class ServiceTestApplication extends Application  {
    @Override
    public Set<Class<?>> getClasses(){
        Set<Class<?>> result = new HashSet<>();
        result.add(ServiceTest.class);
        return Collections.unmodifiableSet(result);
    }
}

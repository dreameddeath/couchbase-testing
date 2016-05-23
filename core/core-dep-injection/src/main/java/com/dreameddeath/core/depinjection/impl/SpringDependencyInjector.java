package com.dreameddeath.core.depinjection.impl;

import com.dreameddeath.core.depinjection.IDependencyInjector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

/**
 * Created by Christophe Jeunesse on 23/05/2016.
 */
public class SpringDependencyInjector implements IDependencyInjector,ApplicationContextAware {
    private ApplicationContext parentContext=null;
    @Override @SuppressWarnings("unchecked")
    public <T> T getBeanOfType(Class<T> clazz) {
        return (T)parentContext.getAutowireCapableBeanFactory().autowire(clazz,AUTOWIRE_BY_TYPE,false);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        parentContext=applicationContext;
    }
}

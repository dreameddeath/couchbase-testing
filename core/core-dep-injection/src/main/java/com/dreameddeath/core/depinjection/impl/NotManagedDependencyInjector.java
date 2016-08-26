package com.dreameddeath.core.depinjection.impl;

import com.dreameddeath.core.depinjection.IDependencyInjector;

/**
 * Created by Christophe Jeunesse on 23/05/2016.
 */
public class NotManagedDependencyInjector implements IDependencyInjector{
    @Override
    public <T> T getBeanOfType(Class<T> clazz) {
        try {
            return clazz.newInstance();
        }
        catch (IllegalAccessException|InstantiationException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T autowireBean(T bean,String beanName) {
        return bean;
    }
}

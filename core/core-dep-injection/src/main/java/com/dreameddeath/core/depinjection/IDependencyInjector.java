package com.dreameddeath.core.depinjection;

/**
 * Created by Christophe Jeunesse on 23/05/2016.
 */
public interface IDependencyInjector {
    <T> T getBeanOfType(Class<T> clazz);

    <T> T autowireBean(T bean,String beanName);
}

package com.dreameddeath.core.depinjection.impl;

import com.dreameddeath.core.depinjection.IDependencyInjector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_NO;

/**
 * Created by Christophe Jeunesse on 23/05/2016.
 */
public class SpringDependencyInjector implements IDependencyInjector,ApplicationContextAware {
    private ApplicationContext parentContext=null;
    @Override @SuppressWarnings("unchecked")
    public <T> T getBeanOfType(Class<T> clazz) {
        return (T)parentContext.getAutowireCapableBeanFactory().createBean(clazz,AUTOWIRE_NO,false);
        //parentContext.getAutowireCapableBeanFactory().autowire(clazz,AUTOWIRE_NO,false);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        parentContext=applicationContext;
    }



    @Override
    public <T> T autowireBean(T bean,String beanName) {
        //parentContext.getAutowireCapableBeanFactory().configureBean(bean,beanName);
        parentContext.getAutowireCapableBeanFactory().autowireBean(bean);
        parentContext.getAutowireCapableBeanFactory().applyBeanPostProcessorsBeforeInitialization(bean,beanName);
        parentContext.getAutowireCapableBeanFactory().applyBeanPostProcessorsAfterInitialization(bean,beanName);
        return bean;
    }
}

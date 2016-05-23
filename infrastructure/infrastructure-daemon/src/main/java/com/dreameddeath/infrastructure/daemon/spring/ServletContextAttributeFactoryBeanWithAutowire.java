package com.dreameddeath.infrastructure.daemon.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.ServletContextAttributeFactoryBean;

import javax.servlet.ServletContext;

/**
 * Created by Christophe Jeunesse on 23/05/2016.
 */
public class ServletContextAttributeFactoryBeanWithAutowire extends ServletContextAttributeFactoryBean implements ApplicationContextAware{
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //Object bean;
        this.applicationContext=applicationContext;
        /*try {
            bean = getObject();
        }
        catch (Exception e){
            throw new RuntimeException("Error during object read",e);
        }
        if (bean!= null) {
            applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,true);
        }*/
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        super.setServletContext(servletContext);
        if(applicationContext!=null){
            Object bean;
            try {
                bean = getObject();
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
            applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        }
    }
}



package com.dreameddeath.infrastructure.daemon.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.ServletContextAttributeFactoryBean;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

/**
 * Created by Christophe Jeunesse on 23/05/2016.
 */
public class ServletContextAttributeFactoryBeanWithAutowire extends ServletContextAttributeFactoryBean implements ApplicationContextAware,BeanNameAware{
    private ApplicationContext applicationContext;
    private String beanName;
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
        //this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
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
            if(bean!=null) {
                applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
            }
        }
    }

    @PostConstruct
    public void postConstruct() {
        if(applicationContext!=null){
            Object bean;
            try {
                bean = getObject();
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
            if(bean!=null) {
                applicationContext.getAutowireCapableBeanFactory().applyBeanPostProcessorsBeforeInitialization(bean, beanName);
            }
        }
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}



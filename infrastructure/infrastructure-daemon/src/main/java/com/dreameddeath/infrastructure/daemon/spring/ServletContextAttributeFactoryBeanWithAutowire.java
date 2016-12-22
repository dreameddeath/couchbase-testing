/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
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



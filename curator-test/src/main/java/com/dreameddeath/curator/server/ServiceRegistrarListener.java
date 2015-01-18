package com.dreameddeath.curator.server;

import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by CEAJ8230 on 14/01/2015.
 */
public class ServiceRegistrarListener implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistrarListener.class);
    private final ServletHolder _servletHolder;

    public ServiceRegistrarListener(ServletHolder holder){
        super();
        _servletHolder = holder;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sc) {

    }

    @Override
    public void contextInitialized(ServletContextEvent sc) {
        try {
            LOG.info(sc.getServletContext().getContextPath());
            //Object obj = WebApplicationContextUtils.getRequiredWebApplicationContext(sc.getServletContext()).getBean("serviceTest");
            //LOG.info(obj.getClass().getName());
            //WebApplicationContextUtils.getRequiredWebApplicatinContext(sc.getServletContext())
            //        .getBean(ServiceRegistrar.class).registerService();
        }
        catch (Exception e) {
            LOG.error("Error Registering Service", e);
            throw new RuntimeException("Exception Registring Service", e);
        }
    }
}

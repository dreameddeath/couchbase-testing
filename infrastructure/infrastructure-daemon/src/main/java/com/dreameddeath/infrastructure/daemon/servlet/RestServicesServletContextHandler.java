/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.infrastructure.daemon.servlet;

import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryManager;
import com.dreameddeath.infrastructure.daemon.services.StandardDaemonRestEndPointDescription;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by Christophe Jeunesse on 28/08/2015.
 */
public class RestServicesServletContextHandler extends AbstractServletContextHandler {
    public static final String SERVICE_DISCOVERER_MANAGER_PARAM_NAME = "serviceDiscovererManager";
    public static final String END_POINT_INFO_SERVLET_PARAM_NAME = "endPointInfo";


    public RestServicesServletContextHandler(AbstractWebServer parentServer,String applicationContextConfig,String path,ServiceDiscoveryManager serviceDiscoveryManager){
        super(parentServer);
        path = ServletUtils.normalizePath(path,false);
        this.setContextPath(path);
        this.setDisplayName("Self registered Web Services");

        //Init Cxf context handler
        ServletHolder cxfHolder = new ServletHolder("rest-service-servlet",CXFServlet.class);
        cxfHolder.setName("Self registered Web Services Cxf Holder");
        cxfHolder.setInitOrder(1);
        cxfHolder.setInitParameter("config-location",applicationContextConfig);
        this.addServlet(cxfHolder, "/*");

        //Setup standardized elements
        this.setAttribute(SERVICE_DISCOVERER_MANAGER_PARAM_NAME, serviceDiscoveryManager);
        this.setAttribute(END_POINT_INFO_SERVLET_PARAM_NAME, new StandardDaemonRestEndPointDescription(parentServer.getServerConnector(),path));
    }
}

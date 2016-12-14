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

package com.dreameddeath.testing.plugin.proxy.service;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.infrastructure.daemon.servlet.AbstractServletContextHandler;
import com.dreameddeath.infrastructure.daemon.servlet.ServletUtils;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by Christophe Jeunesse on 28/08/2015.
 */
public class ProxyTestServletContextHandler extends AbstractServletContextHandler {

    public ProxyTestServletContextHandler(AbstractWebServer parentServer, String path){
        super(parentServer);

        String fullPath;
        if(StringUtils.isNotEmpty(path)){
            fullPath = ServletUtils.normalizePath(path,false);
        }
        else{
            fullPath= ServletUtils.normalizePath(ProxyTestConfigProperties.WEBSERVER_PROXY_SERVICE_TEST_PATH_PREFIX.get(),false);
        }
        this.setContextPath(fullPath);
        this.setDisplayName("Proxy for Service Testing");

        //Init Cxf context handler
        ServletHolder proxyHolder = new ServletHolder("proxy-test-service", ProxyTestingServlet.class);
        proxyHolder.setName("Proxy for Testing services Proxy Holder");
        proxyHolder.setInitOrder(1);
        this.setAttribute(ProxyTestingServlet.SERVLET_CFG_PARAM_BASE_PATH,fullPath);
        this.addServlet(proxyHolder, "/*");
    }
}

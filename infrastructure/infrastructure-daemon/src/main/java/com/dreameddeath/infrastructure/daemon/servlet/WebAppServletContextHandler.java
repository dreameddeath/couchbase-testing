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

import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by Christophe Jeunesse on 28/08/2015.
 */
public class WebAppServletContextHandler extends AbstractServletContextHandler {
    public WebAppServletContextHandler(AbstractWebServer parent,String path,String resourcesPath){
        super(parent);
        this.setContextPath(ServletUtils.normalizePath(path, false));
        this.setDisplayName("Web App Context Handler");
        ServletHolder servletHolder = new ServletHolder(new WebAppServlet());
        servletHolder.setInitOrder(1);
        servletHolder.setName("Web App Holder");
        servletHolder.setInitParameter(WebAppServlet.RESOURCE_PATH_PARAM_NAME, resourcesPath);
        this.addServlet(servletHolder, "/");
    }
}

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

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by Christophe Jeunesse on 28/08/2015.
 */
public class WebJarsServletContextHandler extends ServletContextHandler {
    public WebJarsServletContextHandler(String path,String libsSubPath){
        this.setContextPath(ServletUtils.normalizePath(path, false));
        this.setDisplayName("WebJars deliveries");
        this.setInitParameter(RequireJsServlet.APPS_WEBJARS_LIBS_FULL_PATH,ServletUtils.normalizePath(new String[]{path,libsSubPath}, false));

        ServletHolder requireJsServletHolder = new ServletHolder(new RequireJsServlet());
        requireJsServletHolder.setName("WebJars RequireJs Holder");
        requireJsServletHolder.setInitOrder(1);
        this.addServlet(requireJsServletHolder,"/requirejs_cfg.js");

        ServletHolder webJarsServletHandler = new ServletHolder(new WebJarsServlet());
        webJarsServletHandler.setName("WebJars Servlet Holder");
        webJarsServletHandler.setInitOrder(2);
        this.addServlet(webJarsServletHandler,ServletUtils.normalizePath(libsSubPath,true)+"*");
    }
}

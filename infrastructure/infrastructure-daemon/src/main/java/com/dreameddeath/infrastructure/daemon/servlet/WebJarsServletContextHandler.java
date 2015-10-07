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
    public static String APPS_WEBJARS_LIBS_FOR_TESTING="manual_testing";

    public WebJarsServletContextHandler(String path,String libsSubPath,boolean forTesting){
        this.setContextPath(ServletUtils.normalizePath(path, false));
        this.setDisplayName("WebJars deliveries");
        if(forTesting) {
            this.setInitParameter(APPS_WEBJARS_LIBS_FOR_TESTING, "true");
        }

        ServletHolder requireJsServletHolder = new ServletHolder(new RequireJsServlet());
        requireJsServletHolder.setName("WebJars RequireJs Holder");
        requireJsServletHolder.setInitParameter(RequireJsServlet.APPS_WEBJARS_LIBS_FULL_PATH, ServletUtils.normalizePath(new String[]{path, libsSubPath}, true));
        requireJsServletHolder.setInitOrder(1);
        this.addServlet(requireJsServletHolder,"/requirejs_cfg.js");

        ServletHolder webJarsServletHandler = new ServletHolder(new WebJarsServlet());
        webJarsServletHandler.setName("WebJars Servlet Holder");
        webJarsServletHandler.setInitParameter(WebJarsServlet.PREFIX_WEBJARS_PARAM_NAME, ServletUtils.normalizePath(new String[]{path, libsSubPath}, true));
        if(forTesting){
            webJarsServletHandler.setInitParameter("cacheControl","max-age=0,public");
        }
        webJarsServletHandler.setInitOrder(2);
        this.addServlet(webJarsServletHandler, ServletUtils.normalizePath(libsSubPath, true) + "*");
    }
}

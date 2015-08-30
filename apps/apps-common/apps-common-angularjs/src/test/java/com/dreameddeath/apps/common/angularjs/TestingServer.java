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

package com.dreameddeath.apps.common.angularjs;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.springframework.web.context.ContextLoaderListener;
import org.webjars.RequireJS;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Christophe Jeunesse on 23/08/2015.
 */
public class TestingServer {
    //@Test
    public void runServer() throws Exception{
        Server server = new Server();

        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8080);
        server.addConnector(serverConnector);


        ServletContextHandler webAppContextHandler = new ServletContextHandler();
        {
            //webAppContextHandler.setBaseResource(Resource.newClassPathResource("/META-INF/resources/webapp"));
            webAppContextHandler.setBaseResource(Resource.newResource("src/test/resources/META-INF/resources/webapp"));
            webAppContextHandler.setContextPath("/webapp");
            ServletHolder webAppServletHolder = new ServletHolder(new DefaultServlet());
            webAppServletHolder.setInitParameter("dirAllowed", "false");
            webAppServletHolder.setInitParameter("gzip", "true");
            webAppServletHolder.setInitParameter("etags", "true");
            webAppServletHolder.setInitParameter("maxCacheSize", Integer.toString(5 * 1024 * 1024));
            webAppContextHandler.addServlet(webAppServletHolder, "/");
        }

        ServletContextHandler webJarContextHandler = new ServletContextHandler();
        webJarContextHandler.setContextPath("/webapp/libs");
        {
            ServletHolder webJarServletHolder = new ServletHolder(new DefaultServlet() {
                @Override
                public Resource getResource(String pathInContext) {
                    return Resource.newClassPathResource("/META-INF/resources" + pathInContext);
                }
            });
            webJarServletHolder.setName("webjars_libs");
            webJarServletHolder.setInitParameter("dirAllowed", "false");
            webJarServletHolder.setInitParameter("gzip", "true");
            webJarServletHolder.setInitParameter("etags", "true");
            webJarServletHolder.setInitParameter("maxCacheSize", Integer.toString(5 * 1024 * 1024));
            webJarContextHandler.addServlet(webJarServletHolder, "/webjars/*");
        }
        {
            ServletHolder servletHolder = new ServletHolder(new HttpServlet(){
                private String _response;
                private String _eTag;
                public void init(ServletConfig config) throws ServletException {
                    super.init(config);
                    _response = RequireJS.getSetupJavaScript("/webapp/libs/webjars/");
                    MessageDigest md;
                    try {
                        md = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("MD5 cryptographic algorithm is not available.", e);
                    }
                    byte[] messageDigest = md.digest(_response.getBytes());
                    BigInteger number = new BigInteger(1, messageDigest);
                    // prepend a zero to get a "proper" MD5 hash value
                    StringBuffer sb = new StringBuffer('0');
                    sb.append(number.toString(16));
                    _eTag=sb.toString();
                }

                @Override
                protected void doGet(HttpServletRequest request,
                                     HttpServletResponse response)
                        throws ServletException, IOException {
                    response.setHeader(HttpHeader.ETAG.toString(), _eTag);
                    if(_eTag.equals(request.getHeader(HttpHeader.IF_NONE_MATCH.toString()))){
                        response.setStatus(304);
                    }
                    else {
                        response.setContentType("application/javascript");
                        response.getWriter().println(_response);
                    }
                }
            });
            servletHolder.setName("RequireJsCfg");
            webJarContextHandler.addServlet(servletHolder, "/require_config.js");
        }


        ServletContextHandler webServicesContextHandler = new ServletContextHandler();
        {
            //webJarContextHandler.setBaseResource(Resource.newClassPathResource("/META-INF/resources/webjars"));
            webServicesContextHandler.setContextPath("/apis");
            ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
            webServicesContextHandler.addServlet(cxfHolder, "/*");
            cxfHolder.setInitOrder(1);
            webServicesContextHandler.setInitParameter("contextConfigLocation", "classpath:testrest.applicationContext.xml");
            webServicesContextHandler.addEventListener(new ContextLoaderListener());

        }

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{/*webRequireJsContextHandler,*/webJarContextHandler, webAppContextHandler,webServicesContextHandler});
        server.setHandler(contexts);

        server.start();
        server.join();
    }
}

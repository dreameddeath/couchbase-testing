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

import com.dreameddeath.infrastructure.daemon.springboot.JarFileResourceSpringBoot;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Christophe Jeunesse on 27/08/2015.
 */
public class WebJarsServlet extends DefaultServlet {
    private static final String JARFILE_SEPARATOR="!/";
    private final static Logger LOG = LoggerFactory.getLogger(WebJarsServlet.class);
    public static final String PREFIX_WEBJARS_PARAM_NAME="pathPrefix";
    public static final String DEFAULT_CACHE_SIZE =  Integer.toString(5 * 1024 * 1024);
    private boolean manualTesting=false;
    private String prefix;

    private void assignDefault(ServletConfig config,String name,String value){
        if(config.getServletContext().getInitParameter(name)==null){
            config.getServletContext().setInitParameter(name,value);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        String forTesting = config.getServletContext().getInitParameter(WebJarsServletContextHandler.APPS_WEBJARS_LIBS_FOR_TESTING);
        if ("true".equalsIgnoreCase(forTesting)){
            manualTesting = true;
        }
        prefix = config.getInitParameter(PREFIX_WEBJARS_PARAM_NAME);
        prefix=ServletUtils.normalizePath(prefix,true);

        config.getServletContext().setInitParameter("dirAllowed", "false");
        config.getServletContext().setInitParameter("gzip", "true");
        config.getServletContext().setInitParameter("etags", "true");
        assignDefault(config,"maxCacheSize", DEFAULT_CACHE_SIZE);
        super.init(config);
    }

    @Override
    public Resource getResource(String pathInContext) {
        String originPath = pathInContext;
        pathInContext = pathInContext.replace(prefix,"/webjars/");
        pathInContext = "META-INF/resources" + pathInContext;
        if(manualTesting){
            File srcFile = new File(ServletUtils.LOCAL_WEBAPP_SRC + "/" + pathInContext);
            if (srcFile.exists()) {
                try {
                    return Resource.newResource(srcFile.toURI().toURL());
                }
                catch(MalformedURLException e){
                    throw new RuntimeException("Cannot load local file <"+srcFile.getAbsolutePath()+">",e);
                }
            }
        }

        URL resource = Resource.class.getResource(pathInContext);
        if(resource==null){
            resource = Loader.getResource(this.getClass(), pathInContext);
        }
        if ((resource != null)) {
            String externalForm = resource.toExternalForm();
            if (externalForm.startsWith("jar:file:") && (externalForm.contains(JARFILE_SEPARATOR) && (externalForm.indexOf(JARFILE_SEPARATOR) != externalForm.lastIndexOf(JARFILE_SEPARATOR)))) {
                return new JarFileResourceSpringBoot(resource);
            }
            else {
                return Resource.newResource(resource);
            }
        }
        else{
            return null;
        }
    }
}

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

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.File;
import java.net.MalformedURLException;

/**
 * Created by Christophe Jeunesse on 27/08/2015.
 */
public class WebJarsServlet extends DefaultServlet {
    public static final String PREFIX_WEBJARS_PARAM_NAME="pathPrefix";
    public static final String DEFAULT_CACHE_SIZE =  Integer.toString(5 * 1024 * 1024);
    private boolean _manualTesting=false;
    private String _prefix;

    private void assignDefault(ServletConfig config,String name,String value){
        if(config.getServletContext().getInitParameter(name)==null){
            config.getServletContext().setInitParameter(name,value);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        String forTesting = config.getServletContext().getInitParameter(WebJarsServletContextHandler.APPS_WEBJARS_LIBS_FOR_TESTING);
        if ("true".equalsIgnoreCase(forTesting)){
            _manualTesting = true;
        }
        _prefix = config.getInitParameter(PREFIX_WEBJARS_PARAM_NAME);
        _prefix=ServletUtils.normalizePath(_prefix,true);

        config.getServletContext().setInitParameter("dirAllowed", "false");
        config.getServletContext().setInitParameter("gzip", "true");
        config.getServletContext().setInitParameter("etags", "true");
        assignDefault(config,"maxCacheSize", DEFAULT_CACHE_SIZE);
        super.init(config);
    }

    @Override
    public Resource getResource(String pathInContext) {
        pathInContext = pathInContext.replace(_prefix,"/webjars/");
        pathInContext = "/META-INF/resources" + pathInContext;
        if(_manualTesting){
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
        return Resource.newClassPathResource(pathInContext);
    }
}

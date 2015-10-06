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
import java.net.MalformedURLException;

/**
 * Created by Christophe Jeunesse on 27/08/2015.
 */
public class WebAppServlet extends DefaultServlet {
    public static final String RESOURCE_PATH_PARAM_NAME="pathParam";
    public static final String DEFAULT_CACHE_SIZE =  Integer.toString(5 * 1024 * 1024);

    private Resource _baseResource;

    private void assignDefault(ServletConfig config,String name,String value){
        if(config.getServletContext().getInitParameter(name)==null){
            config.getServletContext().setInitParameter(name,value);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        String path = config.getInitParameter(RESOURCE_PATH_PARAM_NAME);
        if(!path.endsWith("/")){
            path +="/";
        }

        if(path.startsWith("classpath:")){
            _baseResource = Resource.newClassPathResource(path.substring("classpath:".length()));
        }
        else{
            try {
                _baseResource = Resource.newResource(path);
            }
            catch(MalformedURLException e){
                throw new ServletException("Wrong path "+ path,e);
            }
        }

        config.getServletContext().setInitParameter("dirAllowed", "false");
        config.getServletContext().setInitParameter("gzip", "true");
        config.getServletContext().setInitParameter("etags", "true");
        assignDefault(config,"maxCacheSize", DEFAULT_CACHE_SIZE);
        super.init(config);
    }

    @Override
    public Resource getResource(String pathInContext) {
        //pathInContext = pathInContext.replace(_prefix,"/webjars/");
        return _baseResource.getResource(pathInContext);
    }
}

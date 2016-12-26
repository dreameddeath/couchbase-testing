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

package com.dreameddeath.infrastructure.daemon.servlet;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.webjars.RequireJS;
import org.webjars.WebJarAssetLocator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 27/08/2015.
 */
public class RequireJsServlet extends HttpServlet {
    public static String APPS_WEBJARS_LIBS_FULL_PATH="webjar_path";
    private volatile boolean manualTesting=false;
    private volatile String libFullPath;
    private volatile String response;
    private volatile String eTag;

    synchronized private String buildResponse(){
        if(manualTesting){
            try {
                List<String> prefixes = new ArrayList<>();
                prefixes.add(libFullPath);

                Map<String, String> webJars = (new WebJarAssetLocator(
                        WebJarAssetLocator.getFullPathIndex(
                                Pattern.compile(".*"),
                                new URLClassLoader(new URL[]{new File(ServletUtils.LOCAL_WEBAPP_SRC).toURI().toURL()}),
                                Thread.currentThread().getContextClassLoader()
                        )
                )).getWebJars();
                response = RequireJS.generateSetupJavaScript(prefixes, webJars);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Cannot setup testing env", e);
            }
        }
        else {
            response = RequireJS.getSetupJavaScript(libFullPath);
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 cryptographic algorithm is not available.", e);
        }
        byte[] messageDigest = md.digest(response.getBytes(Charset.forName("utf-8")));
        BigInteger number = new BigInteger(1, messageDigest);
        StringBuffer sb = new StringBuffer("0");
        sb.append(number.toString(16));
        eTag=sb.toString();
        return response;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String forTesting = config.getServletContext().getInitParameter(WebJarsServletContextHandler.APPS_WEBJARS_LIBS_FOR_TESTING);
        manualTesting = "true".equalsIgnoreCase(forTesting);
        libFullPath = config.getInitParameter(APPS_WEBJARS_LIBS_FULL_PATH);
        buildResponse();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        String effectiveResponse;
        if(manualTesting){
            effectiveResponse=buildResponse();
        }
        else{
            effectiveResponse=this.response;
        }
        response.setHeader(HttpHeader.ETAG.toString(), eTag);
        if(eTag.equals(request.getHeader(HttpHeader.IF_NONE_MATCH.toString()))){
            response.setStatus(HttpStatus.NOT_MODIFIED_304);
        }
        else {
            response.setContentType("application/javascript");
            response.getWriter().println(effectiveResponse);
        }
    }
}

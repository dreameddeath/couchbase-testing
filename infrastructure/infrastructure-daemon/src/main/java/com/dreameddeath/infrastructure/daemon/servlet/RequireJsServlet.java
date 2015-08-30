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

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
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
 * Created by Christophe Jeunesse on 27/08/2015.
 */
public class RequireJsServlet extends HttpServlet {
    public static String APPS_WEBJARS_LIBS_FULL_PATH="webjar_path";
    private String _response;
    private String _eTag;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        _response = RequireJS.getSetupJavaScript(config.getServletContext().getInitParameter(APPS_WEBJARS_LIBS_FULL_PATH));
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
            response.setStatus(HttpStatus.NOT_MODIFIED_304);
        }
        else {
            response.setContentType("application/javascript");
            response.getWriter().println(_response);
        }
    }
}

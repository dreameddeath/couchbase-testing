/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.testing.plugin.proxy.service;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.infrastructure.daemon.servlet.ServletUtils;
import com.google.common.base.Preconditions;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.proxy.AsyncProxyServlet;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Christophe Jeunesse on 09/12/2016.
 */
public class ProxyTestingServlet extends AsyncProxyServlet  {
    public static final String HEADER_TARGET_HOST = "X-TESTING-TARGET-HOST";
    public static final String HEADER_TARGET_PORT = "X-TESTING-TARGET-PORT";
    public static final String HEADER_TARGET_PATH = "X-TESTING-TARGET-PATH";
    public static final String HEADER_SOURCE_PATH_PREFIX = "X-TESTING-SOURCE-PATH-PREFIX";
    public static final String RESPONSE_HEADER_TARGET_URI = "X-TESTING-TARGET-FULL-URI";
    public static final String SERVLET_CFG_PARAM_BASE_PATH = "testingServletPath";

    private final AtomicBoolean forHttp2=new AtomicBoolean(false);
    private HttpClient http2Client;
    private String pathPrefix = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        forHttp2.set(false);
        super.init(config);
        forHttp2.set(true);
        http2Client=createHttpClient();
        forHttp2.set(false);
        getServletContext().setAttribute(config.getServletName() + ".HttpClientForHTTP2", http2Client);
        pathPrefix = ServletUtils.normalizePath((String)config.getServletContext().getAttribute(SERVLET_CFG_PARAM_BASE_PATH),false);
        Preconditions.checkArgument(StringUtils.isNotEmpty(pathPrefix),"The base path must be provided");
    }

    @Override
    public void destroy() {
        super.destroy();
        if(http2Client!=null){
            try {
                http2Client.stop();
            }
            catch (Exception e){
                _log.warn("Error during http2 client stop",e);
            }
        }
    }



    @Override
    protected HttpClient newHttpClient() {
        HttpClientTransport transport;
        if(forHttp2.get()){
            HTTP2Client http2Client=new HTTP2Client();
            transport=new HttpClientTransportOverHTTP2(http2Client);
        }
        else{
            transport = new HttpClientTransportOverHTTP();
        }

        return new HttpClient(transport,null);
    }

    protected HttpClient getHttpClient(HttpVersion protocolVersion){
        if(HttpVersion.HTTP_2==protocolVersion){
            return http2Client;
        }
        else{
            return getHttpClient();
        }
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final int requestId = getRequestId(request);

        String rewrittenTarget = rewriteTarget(request);

        if (_log.isDebugEnabled())
        {
            StringBuffer uri = request.getRequestURL();
            if (request.getQueryString() != null)
                uri.append("?").append(request.getQueryString());
            if (_log.isDebugEnabled())
                _log.debug("{} rewriting: {} -> {}", requestId, uri, rewrittenTarget);
        }

        if (rewrittenTarget == null)
        {
            onProxyRewriteFailed(request, response);
            return;
        }

        response.addHeader(RESPONSE_HEADER_TARGET_URI,rewrittenTarget);

        HttpVersion version=HttpVersion.fromString(request.getProtocol());
        final Request proxyRequest = getHttpClient(version).newRequest(rewrittenTarget)
                .method(request.getMethod())
                .version(version);

        copyRequestHeaders(request, proxyRequest);

        addProxyHeaders(request, proxyRequest);

        final AsyncContext asyncContext = request.startAsync();
        // We do not timeout the continuation, but the proxy request
        asyncContext.setTimeout(0);
        proxyRequest.timeout(getTimeout(), TimeUnit.MILLISECONDS);

        if (hasContent(request))
            proxyRequest.content(proxyRequestContent(request, response, proxyRequest));
        sendProxyRequest(request, response, proxyRequest);
    }

    @Override
    protected void addViaHeader(Request proxyRequest) {
        proxyRequest.header(HttpHeader.VIA, proxyRequest.getVersion() + getViaHost());
    }


    @Override
    protected String rewriteTarget(HttpServletRequest request) {
        if (!validateDestination(request.getServerName(), request.getServerPort())) {
            return null;
        }

        String targetHost=request.getHeader(HEADER_TARGET_HOST);
        String targetPort=request.getHeader(HEADER_TARGET_PORT);
        String targetPath=request.getHeader(HEADER_TARGET_PATH);
        String sourcePathToRemove=request.getHeader(HEADER_SOURCE_PATH_PREFIX);

        String sourcePath = request.getRequestURI();
        String sourceQuery = request.getQueryString();

        String finalPath=
                ServletUtils.normalizePath(new String[]{targetPath,
                sourcePath.substring(pathPrefix.length()).substring(sourcePathToRemove.length())},request.getRequestURI().endsWith("/"));

        if (sourceQuery != null)
            finalPath += "?" + sourceQuery;
        return URI.create(request.getScheme()+"://"+targetHost+(targetPort!=null?":"+targetPort:"") + "/" + finalPath).normalize().toString();
    }

}

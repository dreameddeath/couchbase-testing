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

package com.dreameddeath.core.http2.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.Address;
import org.apache.cxf.transport.http.Headers;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.transport.https.HttpsURLConnectionInfo;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.client.util.OutputStreamContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.util.Jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.eclipse.jetty.http.HttpHeader.USER_AGENT;

/**
 * Created by Christophe Jeunesse on 20/09/2016.
 */
public class JettyHttp2Conduit extends URLConnectionHTTPConduit {
    public static final String USE_ASYNC = "use.async.http.conduit";

    private final JettyHttp2ConduitFactory parentFactory;

    public JettyHttp2Conduit(Bus b, EndpointInfo ei, EndpointReferenceType t,JettyHttp2ConduitFactory parent) throws IOException {
        super(b, ei, t);
        parentFactory=parent;
    }

    @Override
    protected void setupConnection(Message message, Address address, HTTPClientPolicy csPolicy) throws IOException {
        //
        if (parentFactory.isShutdown()) {
            message.put(USE_ASYNC, Boolean.FALSE);
            super.setupConnection(message, address, csPolicy);
            return;
        }

        URI uri = address.getURI();
        boolean addressChanged = false;
        // need to do some clean up work on the URI address
        String uriString = uri.toString();
        if (uriString.startsWith(JettyHttp2TransportFactory.JETTY_HTTP2_PREFIX)) {
            try {
                uriString = uriString.substring(JettyHttp2TransportFactory.JETTY_HTTP2_PREFIX.length());
                uri = new URI(uriString);
                addressChanged = true;
            } catch (URISyntaxException ex) {
                throw new MalformedURLException("unsupport uri: "  + uriString);
            }
        }

        //Manage default Port
        if(uri.getPort()==-1){
            try{
                uri = new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(),("http".equals(uri.getScheme()) ? 80 : 443),uri.getPath(),uri.getQuery(),uri.getFragment());
            } catch (URISyntaxException ex) {
                throw new MalformedURLException("unsupport uri: "  + uriString);
            }
        }


        String s = uri.getScheme();
        if (!"http".equals(s) && !"https".equals(s)) {
            throw new MalformedURLException("unknown protocol: " + s);
        }

        Object o = message.getContextualProperty(USE_ASYNC);
        if (o == null) {
            o = parentFactory.getUseAsyncPolicy();
        }
        // check tlsClientParameters from message header
        TLSClientParameters clientParameters = message.get(TLSClientParameters.class);
        if (clientParameters == null) {
            clientParameters = tlsClientParameters;
        }

        switch (JettyHttp2ConduitFactory.UseAsyncPolicy.getPolicy(o)) {
            case ALWAYS:
                o = true;
                break;
            case NEVER:
                o = false;
                break;
            case ASYNC_ONLY:
            default:
                o = !message.getExchange().isSynchronous();
                break;
        }

        if (!MessageUtils.isTrue(o)) {
            message.put(USE_ASYNC, Boolean.FALSE);
            super.setupConnection(message, addressChanged ? new Address(uriString, uri) : address, csPolicy);
            return;
        }
        message.put(USE_ASYNC, Boolean.TRUE);

        message.put("http.scheme", uri.getScheme());
        String httpRequestMethod =
                (String)message.get(Message.HTTP_REQUEST_METHOD);
        if (httpRequestMethod == null) {
            httpRequestMethod = "POST";
            message.put(Message.HTTP_REQUEST_METHOD, httpRequestMethod);
        }

        Request request = parentFactory.getClient(clientParameters).newRequest(uri).method(httpRequestMethod).version(HttpVersion.HTTP_2);
        message.put(Request.class,request);
    }

    protected OutputStream createOutputStream(Message message,
                                              boolean needToCacheRequest,
                                              boolean isChunking,
                                              int chunkThreshold) throws IOException {

        if (Boolean.TRUE.equals(message.get(USE_ASYNC))) {
            Request request = message.get(Request.class);
            return new JettyHttp2OutputStreamWrapper(message,needToCacheRequest,isChunking,chunkThreshold,getConduitName(),request.getURI());
        }
        return super.createOutputStream(message, needToCacheRequest, isChunking, chunkThreshold);
    }

    private class JettyHttp2OutputStreamWrapper extends WrappedOutputStream{
        private final HTTPClientPolicy csPolicy;
        private final JettyOutputStreamContentProvider outputStreamJettyContentProvider;
        private final Message message;
        private final Request request;
        private volatile boolean isAsync;
        private final InputStreamResponseListener responseListener;

        protected JettyHttp2OutputStreamWrapper(Message message, boolean possibleRetransmit,
                                                boolean isChunking, int chunkThreshold, String conduitName, URI url) {
            super(message, possibleRetransmit, isChunking, chunkThreshold, conduitName, url);
            csPolicy = getClient(message);
            outputStreamJettyContentProvider=new JettyOutputStreamContentProvider(message);
            this.request = message.get(Request.class);
            request.content(outputStreamJettyContentProvider);
            this.message=message;
            this.responseListener = new InputStreamResponseListener();/*{
                @Override
                public void onComplete(Result result) {
                    super.onComplete(result);
                    markAsReady(result.getResponse());
                }
            };*/
        }


        private void connect(boolean withOutput){
            request.method((String)message.get(Message.HTTP_REQUEST_METHOD))
                    .followRedirects(true)
                    .onResponseHeaders(this::markAsReady)
                    .send(responseListener);
            if(!withOutput){
                outputStreamJettyContentProvider.close();
            }
        }



        private void markAsReady(Response res) {
            if (isAsync) {
                //got a response, need to start the response processing now
                try {
                    handleResponseOnWorkqueue(false, true);
                    isAsync = false; // don't trigger another start on next block. :-)
                } catch (Exception ex) {
                    //ignore, we'll try again on the next consume;
                }
            }
        }

        /*
        *
        *  Manage outgoing side (without retransmit)
        *
         */
        @Override
        protected void handleNoOutput() throws IOException {
            connect(false);

        }

        @Override
        protected void setProtocolHeaders() throws IOException {
            Headers h = new Headers(message);
            request.getHeaders().put(HttpHeaderHelper.CONTENT_TYPE,h.determineContentType());
            boolean addHeaders = MessageUtils.isTrue(message.getContextualProperty(Headers.ADD_HEADERS_PROPERTY));

            for (Map.Entry<String, List<String>> header : h.headerMap().entrySet()) {
                if (HttpHeaderHelper.CONTENT_TYPE.equalsIgnoreCase(header.getKey())) {
                    continue;
                }
                if (addHeaders || HttpHeaderHelper.COOKIE.equalsIgnoreCase(header.getKey())) {
                    for(String val:header.getValue()) {
                        request.getHeaders().add(header.getKey(), val);
                    }
                } else if (!HttpHeaderHelper.CONTENT_LENGTH.equalsIgnoreCase(header.getKey())) {
                    request.getHeaders().put(header.getKey(),header.getValue());
                }
            }
            if (!request.getHeaders().containsKey(USER_AGENT.asString())) {
                request.getHeaders().put(USER_AGENT.asString(), HTTP2Client.class.getName() + "/" + Jetty.VERSION);
            }
        }


        @Override
        protected void setupWrappedStream() throws IOException {
            connect(true);
            wrappedStream=outputStreamJettyContentProvider.getOutputStream();
            // If we need to cache for retransmission, store data in a
            // CacheAndWriteOutputStream. Otherwise write directly to the output stream.
            if (cachingForRetransmission) {
                cachedStream = new CacheAndWriteOutputStream(wrappedStream);
                wrappedStream = cachedStream;
            }
        }

        @Override
        protected void setFixedLengthStreamingMode(int i) {
            // Here we can set the Content-Length
            request.getHeaders().put("Content-Length", Integer.toString(i));
        }

        @Override
        protected HttpsURLConnectionInfo getHttpsURLConnectionInfo() throws IOException {
            if ("http".equals(outMessage.get("http.scheme"))) {
                return null;
            }
            connect(true);

            String method = (String)outMessage.get(Message.HTTP_REQUEST_METHOD);
            String cipherSuite = null;
            Certificate[] localCerts = null;
            Principal principal = null;
            Certificate[] serverCerts = null;
            Principal peer = null;
            /* TODO manage SSLSession retrieval
                if (session != null) {
                cipherSuite = session.getCipherSuite();
                localCerts = session.getLocalCertificates();
                principal = session.getLocalPrincipal();
                serverCerts = session.getPeerCertificates();
                peer = session.getPeerPrincipal();
            }*/

            return new HttpsURLConnectionInfo(url, method, cipherSuite, localCerts, principal, serverCerts, peer);
        }

        /*
        *
        * Response management
        *
         */
        private Response getHttpResponse() throws IOException {
            try {
                return responseListener.get(csPolicy.getReceiveTimeout(), TimeUnit.MILLISECONDS) ;
            } catch (TimeoutException|InterruptedException|ExecutionException e) {
                request.abort(e);
                throw new IOException(e);
            }

        }
         private String readHeaders(Headers h) throws IOException {
             HttpFields httpFields = getHttpResponse().getHeaders();
             httpFields.forEach(httpField -> h.headerMap().put(httpField.getName(), Arrays.asList(httpField.getValues())));
             return httpFields.get(HttpHeaderHelper.CONTENT_TYPE);
        }


        @Override
        protected int getResponseCode() throws IOException{
            return  getHttpResponse().getStatus();
        }
        @Override
        protected String getResponseMessage() throws IOException{
            return getHttpResponse().getReason();
        }
        @Override
        protected void updateResponseHeaders(Message inMessage) throws IOException{
            Headers h = new Headers(inMessage);
            inMessage.put(Message.CONTENT_TYPE, readHeaders(h));
            cookies.readFromHeaders(h);
        }
        @Override
        protected void handleResponseAsync() throws IOException {
            isAsync = true;
        }
        @Override
        protected void closeInputStream() throws IOException{
            responseListener.getInputStream().close();
        }
        @Override
        protected boolean usingProxy(){
            return false;//TODO improve
        }
        @Override
        protected InputStream getInputStream() throws IOException{
            return responseListener.getInputStream();
        }
        @Override
        protected InputStream getPartialResponse() throws IOException{
            InputStream in = null;
            int responseCode = getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_ACCEPTED
                    || responseCode == HttpURLConnection.HTTP_OK) {

                HttpFields headers = getHttpResponse().getHeaders();
                long cli = headers.getLongField(HttpHeaderHelper.CONTENT_LENGTH);
                boolean isChunked = HttpHeaderHelper.CHUNKED.equalsIgnoreCase(headers.get(HttpHeaderHelper.TRANSFER_ENCODING));
                boolean isEofTerminated = HttpHeaderHelper.CLOSE.equalsIgnoreCase(headers.get(HttpHeaderHelper.CONNECTION));
                if (cli > 0) {
                    in = getInputStream();
                } else if (isChunked || isEofTerminated) {
                    // ensure chunked or EOF-terminated response is non-empty
                    try {
                        PushbackInputStream pin =
                                new PushbackInputStream(getInputStream());
                        int c = pin.read();
                        if (c != -1) {
                            pin.unread((byte)c);
                            in = pin;
                        }
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
            }
            return in;
        }


        /*
        *
        * Retransmit management
        *
         */
        @Override
        protected void setupNewConnection(String newURL) throws IOException{
            throw new IllegalStateException("Should not occurs");
        }
        @Override
        protected void retransmitStream() throws IOException{
            throw new IllegalStateException("Should not occurs");
        }
        @Override
        protected void updateCookiesBeforeRetransmit() throws IOException{
            throw new IllegalStateException("Should not occurs");
        }


        @Override
        public void thresholdReached() throws IOException {

        }

        private class JettyOutputStreamContentProvider extends OutputStreamContentProvider{
            private final OutputStreamWrapper wrapper;

            public JettyOutputStreamContentProvider(Message message) {
                super();
                this.wrapper = new OutputStreamWrapper(super.getOutputStream());
            }

            @Override
            public OutputStream getOutputStream() {
                return wrapper;
            }

            private class OutputStreamWrapper extends OutputStream{
                private final OutputStream wrapperOutputStream;

                public OutputStreamWrapper(OutputStream wrapperOutputStream) {
                    this.wrapperOutputStream = wrapperOutputStream;
                }

                @Override
                public void write(int b) throws IOException {
                    wrapperOutputStream.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    wrapperOutputStream.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    wrapperOutputStream.write(b, off, len);
                }

                @Override
                public void flush() throws IOException {
                    wrapperOutputStream.flush();
                }

                @Override
                public void close() throws IOException {
                    wrapperOutputStream.close();
                }
            }
        }

    }


}

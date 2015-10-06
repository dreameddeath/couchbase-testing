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

package com.dreameddeath.infrastructure.daemon.webserver;

import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.component.LifeCycle;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Created by Christophe Jeunesse on 21/08/2015.
 */
public abstract class AbstractWebServer {
    public static final String GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME = "globalCuratorClient";
    public static final String GLOBAL_DAEMON_LIFE_CYCLE_PARAM_NAME = "daemonLifeCycle";
    public static final String GLOBAL_DAEMON_PARAM_NAME = "daemon";
    private final AbstractDaemon _parentDaemon;
    private final String _name;
    private final Server _webServer;
    private final ServerConnector _serverConnector;

    private String getAddress(String address,String networkInterfaceName){
        if(address!=null){
            return address;
        }
        else if(networkInterfaceName!=null){
            try {
                NetworkInterface networkInterface = NetworkInterface.getByName(networkInterfaceName);
                InetAddress inetAddress = networkInterface.getInetAddresses().nextElement();
                if(inetAddress!=null){
                    return inetAddress.getHostAddress();
                }
            }
            catch(Exception e){
                //Ignore
            }
        }
        return null;
    }

    public AbstractWebServer(Builder builder) {
        _parentDaemon = builder._daemon;
        _name = builder._name;
        _parentDaemon.getDaemonLifeCycle().addLifeCycleListener(new WebServerDaemonLifeCycleListner(this,builder._isRoot));
        _webServer = new Server();
        _serverConnector = new ServerConnector(_webServer);

        String address = getAddress(builder._address, builder._interfaceName);
        if(address==null){
            address = getAddress(DaemonConfigProperties.DAEMON_WEBSERVER_ADDRESS.get(),DaemonConfigProperties.DAEMON_WEBSERVER_INTERFACE.get());
        }
        if(address!=null){
            _serverConnector.setHost(address);
        }

        int port = builder._port;
        if(port==0){
            port = DaemonConfigProperties.DAEMON_WEBSERVER_PORT.get();
        }
        if(port!=0){
            _serverConnector.setPort(port);
        }

        _webServer.addConnector(_serverConnector);
    }

    public AbstractDaemon getParentDaemon() {
        return _parentDaemon;
    }

    public Server getWebServer() {
        return _webServer;
    }

    public String getName() {
        return _name;
    }

    public ServerConnector getServerConnector() {
        return _serverConnector;
    }

    public void start() throws Exception{
        _webServer.start();
    }

    public void stop() throws Exception{
        _webServer.stop();
    }

    public void join() throws Exception{
        _webServer.join();
    }

    public Status getStatus(){
        return Status.fromStateString(_webServer.getState());
    }

    public enum Status{
        UNKNOWN,
        FAILED,
        STOPPED,
        STARTING,
        STARTED,
        RUNNING,
        STOPPING;

        public static Status fromStateString(String stateStr){
            switch (stateStr){
                case "RUNNING":return RUNNING;
                case "STOPPED":return STOPPED;
                case "FAILED":return FAILED;
                case "STARTING":return STARTING;
                case "STARTED":return STARTED;
                case "STOPPING":return STOPPING;
                default:return UNKNOWN;
            }
        }
    }

    public LifeCycle getLifeCycle(){
        return _webServer;
    }


    public static Builder builder(){
        return new Builder();
    }

    public static class Builder<T extends AbstractWebServer.Builder> {
        private AbstractDaemon _daemon;
        private String _name;
        private String _address=null;
        private String _interfaceName=null;
        private boolean _isRoot=false;
        private int _port=0;

        public T withAddress(String address) {
            _address = address;
            return (T)this;
        }

        public T withInterfaceName(String interfaceName) {
            _interfaceName = interfaceName;
            return (T)this;
        }

        public T withIsRoot(boolean isRoot) {
            _isRoot = isRoot;

            return (T) this;
        }

        public T withName(String name) {
            _name = name;
            return (T)this;
        }

        public T withDaemon(AbstractDaemon daemon) {
            _daemon = daemon;
            return (T)this;
        }

        public T withPort(int port) {
            _port = port;
            return (T)this;
        }
    }
}

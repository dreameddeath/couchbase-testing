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

import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class WebServerDaemonLifeCycleListener implements IDaemonLifeCycle.Listener {
    private final static Logger LOG = LoggerFactory.getLogger(WebServerDaemonLifeCycleListener.class);
    private final AbstractWebServer webServer;
    private final boolean isRootWebServer;

    public WebServerDaemonLifeCycleListener(AbstractWebServer webServer, boolean isRootWebServer) {
        this.webServer = webServer;
        this.isRootWebServer = isRootWebServer;
    }

    @Override
    public int getRank() {
        return 1000;
    }

    @Override
    public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {
        if(isRootWebServer){
            try {
                webServer.start();
            }
            catch(Exception e){
                throw new RuntimeException("Error during starting of "+webServer.getName(), e);
            }
        }
    }

    @Override
    public void lifeCycleStarted(IDaemonLifeCycle lifeCycle) {
        if(!isRootWebServer){
            try {
                webServer.start();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {
        try {
            webServer.stop();
        }
        catch (Exception e){
            LOG.error("Error during failure stop of "+webServer.getName(),e);
        }
    }

    @Override
    public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {
        if(!isRootWebServer){
            try {
                webServer.stop();
            }
            catch(Exception e){
                throw new RuntimeException("Error during halting of "+webServer.getName(),e);
            }
        }
    }

    @Override
    public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
        if(!isRootWebServer){
            try {
                webServer.stop();
            }
            catch(Exception e){
                throw new RuntimeException("Error during stopping of "+webServer.getName(),e);
            }
        }
    }

    @Override
    public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
        if(isRootWebServer){
            try {
                webServer.stop();
            }
            catch(Exception e){
                throw new RuntimeException("Error during stopping of root webserver "+webServer.getName(),e);
            }
        }
    }
}

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

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class WebServerDaemonLifeCycleListner implements IDaemonLifeCycle.Listener {
    private final StandardWebServer _standardWebServer;
    private final boolean _isRootWebServer;

    public WebServerDaemonLifeCycleListner(StandardWebServer standardWebServer, boolean isRootWebServer) {
        _standardWebServer = standardWebServer;
        _isRootWebServer = isRootWebServer;
    }

    @Override
    public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {
        if(_isRootWebServer){
            try {
                _standardWebServer.start();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void lifeCycleStarted(IDaemonLifeCycle lifeCycle) {
        if(!_isRootWebServer){
            try {
                _standardWebServer.start();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {

    }

    @Override
    public void lifeCycleReload(IDaemonLifeCycle lifeCycle) {
        if(!_isRootWebServer){
            try {
                _standardWebServer.stop();
                _standardWebServer.start();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {
        if(!_isRootWebServer){
            try {
                _standardWebServer.stop();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
        if(!_isRootWebServer){
            try {
                _standardWebServer.stop();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
        if(_isRootWebServer){
            try {
                _standardWebServer.stop();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}

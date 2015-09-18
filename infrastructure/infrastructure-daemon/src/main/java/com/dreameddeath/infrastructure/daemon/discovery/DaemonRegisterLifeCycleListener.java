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

package com.dreameddeath.infrastructure.daemon.discovery;

import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by Christophe Jeunesse on 16/09/2015.
 */
public class DaemonRegisterLifeCycleListener implements IDaemonLifeCycle.Listener {
    private final DaemonDiscovery _daemonDiscovery;

    public DaemonRegisterLifeCycleListener(CuratorFramework framework){
        _daemonDiscovery = new DaemonDiscovery(framework);
    }

    @Override
    public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {
        try {
            _daemonDiscovery.register(lifeCycle.getDaemon());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleStarted(IDaemonLifeCycle lifeCycle) {
        try {
            _daemonDiscovery.update(lifeCycle.getDaemon());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {
        try {
            _daemonDiscovery.update(lifeCycle.getDaemon());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleReload(IDaemonLifeCycle lifeCycle) {
        try {
            _daemonDiscovery.update(lifeCycle.getDaemon());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {
        try {
            _daemonDiscovery.update(lifeCycle.getDaemon());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
        try {
            _daemonDiscovery.update(lifeCycle.getDaemon());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
        try {
            _daemonDiscovery.unregister(lifeCycle.getDaemon());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}

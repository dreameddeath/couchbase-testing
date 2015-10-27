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

package com.dreameddeath.infrastructure.daemon.registrar;

import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.model.DaemonInfo;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by Christophe Jeunesse on 16/09/2015.
 */
public class DaemonRegisterLifeCycleListener implements IDaemonLifeCycle.Listener {
    private final DaemonRegistrar daemonRegistrar;

    public DaemonRegisterLifeCycleListener(CuratorFramework framework){
        daemonRegistrar = new DaemonRegistrar(framework);
    }

    @Override
    public int getRank() {
        return 10000;
    }

    @Override
    public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {
        try {
            daemonRegistrar.register(new DaemonInfo(lifeCycle.getDaemon()));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleStarted(IDaemonLifeCycle lifeCycle) {
        try {
            daemonRegistrar.update(new DaemonInfo(lifeCycle.getDaemon()));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {
        try {
            daemonRegistrar.update(new DaemonInfo(lifeCycle.getDaemon()));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {
        try {
            daemonRegistrar.update(new DaemonInfo(lifeCycle.getDaemon()));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
        try {
            daemonRegistrar.update(new DaemonInfo(lifeCycle.getDaemon()));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
        try {
            daemonRegistrar.deregister(new DaemonInfo(lifeCycle.getDaemon()));
            daemonRegistrar.close();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}

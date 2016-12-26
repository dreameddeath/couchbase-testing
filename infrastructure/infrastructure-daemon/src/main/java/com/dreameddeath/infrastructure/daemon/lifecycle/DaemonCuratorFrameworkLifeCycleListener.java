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

package com.dreameddeath.infrastructure.daemon.lifecycle;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;

/**
 * Created by Christophe Jeunesse on 26/12/2016.
 */
public class DaemonCuratorFrameworkLifeCycleListener implements IDaemonLifeCycle.Listener {
    private final CuratorFramework curatorFramework;
    public DaemonCuratorFrameworkLifeCycleListener(CuratorFramework curatorClient) {
        this.curatorFramework=curatorClient;
    }

    @Override
    public int getRank() {
        return 0;
    }

    @Override
    public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStarted(IDaemonLifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {
        lifeCycleStopped(lifeCycle);
    }

    @Override
    public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
        if (curatorFramework.getState().equals(CuratorFrameworkState.STARTED)) {
            curatorFramework.close();
        }
    }
}

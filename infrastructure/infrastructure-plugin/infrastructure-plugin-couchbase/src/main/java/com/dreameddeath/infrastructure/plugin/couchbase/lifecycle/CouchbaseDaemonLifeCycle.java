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

package com.dreameddeath.infrastructure.plugin.couchbase.lifecycle;

import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;

/**
 * Created by Christophe Jeunesse on 22/10/2015.
 */
public class CouchbaseDaemonLifeCycle implements IDaemonLifeCycle.Listener {
    private final CouchbaseDaemonPlugin plugin;

    public CouchbaseDaemonLifeCycle(CouchbaseDaemonPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public int getRank() {
        return 100;
    }

    @Override
    public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStarted(IDaemonLifeCycle lifeCycle) {
        try {
            plugin.getBucketFactory().start();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        plugin.getBucketFactory().setAutoStart(true);
    }

    @Override
    public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {
        try {
            plugin.getBucketFactory().close();
        }
        catch (Exception e){
            //
        }
        finally {
            plugin.getClusterFactory().close();
        }

    }

    @Override
    public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {
        plugin.getBucketFactory().setAutoStart(false);
    }

    @Override
    public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
        plugin.getBucketFactory().setAutoStart(false);
    }

    @Override
    public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
        plugin.getClusterFactory().close();
    }
}

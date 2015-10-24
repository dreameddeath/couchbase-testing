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

package com.dreameddeath.infrastructure.daemon.couchbase;

import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;

/**
 * Created by Christophe Jeunesse on 22/10/2015.
 */
public class CouchbaseDaemonLifeCycle implements IDaemonLifeCycle.Listener {
    private final DaemonCouchbaseFactories daemonCouchbaseFactories;

    public CouchbaseDaemonLifeCycle(DaemonCouchbaseFactories daemonCouchbaseFactories){
        this.daemonCouchbaseFactories = daemonCouchbaseFactories;
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
            daemonCouchbaseFactories.getBucketFactory().start();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        daemonCouchbaseFactories.getBucketFactory().setAutoStart(true);
    }

    @Override
    public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {
        try {
            this.daemonCouchbaseFactories.getBucketFactory().close();
        }
        catch (Exception e){
            //
        }
        finally {
            this.daemonCouchbaseFactories.getClusterFactory().close();
        }

    }


    @Override
    public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {
        daemonCouchbaseFactories.getBucketFactory().setAutoStart(false);

    }

    @Override
    public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
        daemonCouchbaseFactories.getBucketFactory().setAutoStart(false);
    }

    @Override
    public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
        this.daemonCouchbaseFactories.getClusterFactory().close();
    }
}

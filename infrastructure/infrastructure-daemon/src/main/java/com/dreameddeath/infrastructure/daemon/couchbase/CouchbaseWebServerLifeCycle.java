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

import org.eclipse.jetty.util.component.LifeCycle;

/**
 * Created by Christophe Jeunesse on 22/10/2015.
 */
public class CouchbaseWebServerLifeCycle implements LifeCycle.Listener {
    private final WebServerCouchbaseFactories couchbaseFactories;

    public CouchbaseWebServerLifeCycle(WebServerCouchbaseFactories couchbaseFactories) {
        this.couchbaseFactories = couchbaseFactories;
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {

    }

    @Override
    public void lifeCycleStarted(LifeCycle event) {

    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        couchbaseFactories.close();
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {

    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
        couchbaseFactories.close();
    }
}

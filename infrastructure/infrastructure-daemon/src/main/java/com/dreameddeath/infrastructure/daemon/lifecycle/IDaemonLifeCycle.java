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

package com.dreameddeath.infrastructure.daemon.lifecycle;

import com.dreameddeath.infrastructure.daemon.AbstractDaemon;

import java.util.EventListener;

/**
 * Created by Christophe Jeunesse on 13/08/2015.
 */
public interface IDaemonLifeCycle {

    void start() throws Exception;
    void stop() throws Exception;
    void reload() throws Exception;

    boolean isRunning();
    boolean isStarted();
    boolean isStarting();
    boolean isStopping();
    boolean isStopped();
    boolean isFailed();

    void addLifeCycleListener(Listener listener);
    void removeLifeCycleListener(Listener listener);

    AbstractDaemon getDaemon();

    interface Listener extends EventListener {
        void lifeCycleStarting(IDaemonLifeCycle lifeCycle);

        void lifeCycleStarted(IDaemonLifeCycle lifeCycle);

        void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception);

        void lifeCycleReload(IDaemonLifeCycle lifeCycle);

        void lifeCycleStopping(IDaemonLifeCycle lifeCycle);

        void lifeCycleStopped(IDaemonLifeCycle lifeCycle);
    }
}
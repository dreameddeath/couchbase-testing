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

package com.dreameddeath.infrastructure.plugin.process;

import org.eclipse.jetty.util.component.LifeCycle;

/**
 * Created by Christophe Jeunesse on 11/03/2016.
 */
public class ProcessesWebServerLifeCycle implements LifeCycle.Listener{
    private final ProcessesWebServerPlugin plugin;

    public ProcessesWebServerLifeCycle(ProcessesWebServerPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {

    }

    @Override
    public void lifeCycleStarted(LifeCycle event) {
        plugin.getExecutorClientsPreInit().init();
    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        plugin.getExecutorClientFactory().cleanup();
        plugin.getProcessingServiceFactory().cleanup();
        plugin.getExecutorServiceFactory().cleanup();
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {

    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
        plugin.getExecutorClientFactory().cleanup();
        plugin.getProcessingServiceFactory().cleanup();
        plugin.getExecutorServiceFactory().cleanup();
        plugin.getExecutorClientsPreInit().cleanup();
    }
}

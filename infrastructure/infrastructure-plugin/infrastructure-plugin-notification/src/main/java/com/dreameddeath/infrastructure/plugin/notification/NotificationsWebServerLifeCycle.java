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

package com.dreameddeath.infrastructure.plugin.notification;

import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 20/08/2016.
 */
public class NotificationsWebServerLifeCycle implements LifeCycle.Listener {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationsWebServerLifeCycle.class);
    private NotificationWebServerPlugin parentPlugin;
    public NotificationsWebServerLifeCycle(NotificationWebServerPlugin parentPlugin) {
        this.parentPlugin=parentPlugin;
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
        try {
            parentPlugin.getListenerDiscoverer().start();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        parentPlugin.getEventBus().start();
    }

    @Override
    public void lifeCycleStarted(LifeCycle event) {
    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        parentPlugin.getEventBus().stop();
        try{
            parentPlugin.getListenerDiscoverer().stop();
        }
        catch (Exception e){
            LOG.error("Emergency stop failure",e);
        }
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {

    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
        parentPlugin.getEventBus().stop();
        parentPlugin.getListenerRegistrar().close();
        try {
            parentPlugin.getListenerDiscoverer().stop();
        }
        catch (Exception e){
            LOG.error("Stop failure",e);
        }
    }
}

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

package com.dreameddeath.infrastructure.daemon.plugin;

import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;

/**
 * Created by Christophe Jeunesse on 19/12/2015.
 */
public class AbstractWebServerPlugin extends AbstractPlugin {
    private final AbstractWebServer parentWebServer;

    public AbstractWebServerPlugin(AbstractWebServer server){
        this.parentWebServer = server;
    }

    public AbstractWebServer getParentWebServer() {
        return parentWebServer;
    }

    public AbstractDaemon getParentDaemon(){
        return parentWebServer.getParentDaemon();
    }
}

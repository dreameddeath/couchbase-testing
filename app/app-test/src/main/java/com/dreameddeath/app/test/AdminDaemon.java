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

package com.dreameddeath.app.test;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.AuthorizeAllUser;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.webserver.ProxyWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.WebAppWebServer;

/**
 * Created by Christophe Jeunesse on 16/12/2015.
 */
public class AdminDaemon extends AbstractDaemon {
    public AdminDaemon(){
        super(AdminDaemon.builder()
                .withName("AdminDaemon")
                .withUserFactory(token -> {
                    if(StringUtils.isEmpty(token)){
                        return AnonymousUser.INSTANCE;
                    }
                    else {
                        return AuthorizeAllUser.INSTANCE;
                    }
                })

        );
        this.addWebServer(WebAppWebServer.builder()
                .withName("apps-admin-tests")
                .withApplicationContextConfig("/META-INF/spring/testadmin.applicationContext.xml")
                .withApiPath("/apis")
        );
        addWebServer(ProxyWebServer.builder()
                .withName("proxy")
                .withDiscoverDomain(DaemonConfigProperties.DAEMON_ADMIN_SERVICES_DOMAIN.get()));
    }

    public static void main(String [ ] args) throws Exception
    {
        AbstractDaemon daemon = new AdminDaemon();
        daemon.startAndJoin();
    }
}

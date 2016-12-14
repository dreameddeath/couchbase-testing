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

package com.dreameddeath.testing.plugin.proxy.service;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by Christophe Jeunesse on 09/12/2016.
 */
@ConfigPropertyPackage(name="proxy-service-test",domain = "testing",descr = "All common properties for service testing classes")
public class ProxyTestConfigProperties {
    @ConfigPropertyDoc(
            name="testing.webserver.proxy-test-service.path-prefix",
            descr = "defines the testing proxy default path ",
            defaultValue = "proxy-test-service",
            examples = {"proxy-test-service","proxy"}
    )
    public static final StringConfigProperty WEBSERVER_PROXY_SERVICE_TEST_PATH_PREFIX = ConfigPropertyFactory.getStringProperty("testing.webserver.proxy-test-service.path-prefix", "proxy-test-service");

}

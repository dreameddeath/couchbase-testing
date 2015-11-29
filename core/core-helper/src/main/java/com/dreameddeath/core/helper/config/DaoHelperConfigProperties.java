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

package com.dreameddeath.core.helper.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by Christophe Jeunesse on 23/10/2015.
 */
@ConfigPropertyPackage(name="core-helper",domain = "dao",descr = "All common properties for daemon classes")
public class DaoHelperConfigProperties {
    @ConfigPropertyDoc(
            name="core.helper.dao.services.read.domain",
            descr = "defines the domain of dao services for Service Discovery Register",
            defaultValue = "dao.read",
            examples = {"dao.read"}
    )
    public static final StringConfigProperty DAO_READ_SERVICES_DOMAIN = ConfigPropertyFactory.getStringProperty("core.helper.dao.services.read.domain", "dao.read");

    @ConfigPropertyDoc(
            name="core.helper.dao.services.write.domain",
            descr = "defines the domain of dao services for Service Discovery Register",
            defaultValue = "dao.write",
            examples = {"dao.write"}
    )
    public static final StringConfigProperty DAO_WRITE_SERVICES_DOMAIN = ConfigPropertyFactory.getStringProperty("core.helper.dao.services.write.domain", "dao.write");

}

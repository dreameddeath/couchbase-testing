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

package com.dreameddeath.core.service.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.ConfigPropertyWithTemplateName;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by Christophe Jeunesse on 26/11/2015.
 */
@ConfigPropertyPackage(name="service",domain = "core",descr = "All common properties for services core classes")
public class ServiceConfigProperties {
    @ConfigPropertyDoc(
            name="service.domain.root.path",
            descr = "defines the root path for all domains",
            defaultValue = "/services",
            examples = {"/services/domains"}
    )
    public static final StringConfigProperty SERVICES_DISCOVERY_ROOT_PATH = ConfigPropertyFactory.getStringProperty("service.domains.root.path", "/services");


    @ConfigPropertyDoc(
            name="service.domain.{name}.description",
            descr = "defines the description of the domain"
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> SERVICE_DOMAIN_DESCRIPTION =
            ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class,"service.domain.{name}.description",(String)null);


}

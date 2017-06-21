/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.infrastructure.plugin.query.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.ConfigPropertyWithTemplateName;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by Christophe Jeunesse on 10/03/2016.
 */
@ConfigPropertyPackage(domain = "infrastructure",name = "queries",descr = "Configuration properties for queries for Infrastructure")
public class InfrastructureQueryPluginConfigProperties {

    @ConfigPropertyDoc(
            name="infrastructure.daemon.plugin.query.remote.domain.{name}.map",
            descr = "defines the zookeeper registering path for a given remote query domain"
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> REMOTE_SERVICE_FOR_DOMAIN = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class,"infrastructure.daemon.plugin.query.remote.domain.{name}.map",(String)null);


}

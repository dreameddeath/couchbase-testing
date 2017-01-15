/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.elasticsearch.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.ConfigPropertyWithTemplateName;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.LongConfigProperty;
import com.dreameddeath.core.config.impl.StringListConfigProperty;

/**
 * Created by Christophe Jeunesse on 10/10/2015.
 */
@ConfigPropertyPackage(name="elasticsearch",domain = "core",descr = "All common properties for elastic search classes")
public class ElasticSearchConfigProperties {
    @ConfigPropertyDoc(
            name="core.elasticsearch.cluster.{clusterName}.addresses",
            descr = "defines the list of elasticsearch servers in the cluster (defined by an address with potentially the port)",
            examples = {"192.168.1.1,elasticsearch.1.test.com,toto:port"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringListConfigProperty> ELASTICSEARCH_CLUSTER_ADDRESSES = ConfigPropertyFactory.getTemplateNameConfigProperty(StringListConfigProperty.class,"core.elasticsearch.cluster.{clusterName}.addresses", (String) null);

    @ConfigPropertyDoc(
            name="core.elasticsearch.default.port",
            descr = "defines the default elastic search port",
            defaultValue = "9300",
            examples = {"9300"}
    )
    public static final LongConfigProperty ELASTICSEARCH_DEFAULT_PORT = ConfigPropertyFactory.getLongProperty("core.elasticsearch.default.port", 9300);
}

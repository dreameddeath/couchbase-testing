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

package com.dreameddeath.couchbase.core.catalog.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.StringConfigProperty;

@ConfigPropertyPackage(name="catalog",domain = "core",descr = "All common properties for catalog classes")
public class CatalogConfigProperties {
    @ConfigPropertyDoc(
            name="core.catalog.cache.size",
            descr = "catalog cache size (size estimated based on catalog elements storage)",
            defaultValue = "1M",
            examples = {"2000,20k,2g"}
    )
    public static final StringConfigProperty CATALOG_CACHE_SIZE = ConfigPropertyFactory.getStringProperty("core.catalog.cache.size","1M");

    @ConfigPropertyDoc(
            name="core.catalog.domain",
            descr = "catalog domain name",
            defaultValue = "catalog",
            examples = {"catalog,cat"}
    )
    public static final StringConfigProperty CATALOG_DOMAIN = ConfigPropertyFactory.getStringProperty("core.catalog.domain", "catalog");

    @ConfigPropertyDoc(
            name="core.catalog.user",
            descr = "catalog database session user name",
            defaultValue = "catalog",
            examples = {"catalog,cat"}
    )
    public static final StringConfigProperty CATALOG_USER_ID = ConfigPropertyFactory.getStringProperty("core.catalog.user", "catalog");

    @ConfigPropertyDoc(
            name="core.catalog.state",
            descr = "catalog expected state",
            defaultValue = "PROD",
            examples = {"PROD,DEV,PREPROD,QUALIF"}
    )
    public static final StringConfigProperty CATALOG_STATE = ConfigPropertyFactory.getStringProperty("core.catalog.state", "PROD");
}
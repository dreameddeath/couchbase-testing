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

package com.dreameddeath.core.dao.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.ConfigPropertyWithTemplateName;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by Christophe Jeunesse on 11/10/2015.
 */
@ConfigPropertyPackage(name="dao",domain = "core",descr = "All common properties for couchbase dao classes")
public class CouchbaseDaoConfigProperties {
    @ConfigPropertyDoc(
            name="core.couchbase.dao.{domain}.bucket",
            descr = "defines the attached couchbase bucket name for given domain",
            examples = {"default_bucket"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_DAO_DOMAIN_BUCKET_NAME = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class, "core.couchbase.dao.{domain}.bucket", (String) null);



    @ConfigPropertyDoc(
            name="core.couchbase.dao.{domain}.{name}.bucket",
            descr = "defines the attached couchbase bucket name for given domain/name",
            examples = {"default bucket"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_DAO_BUCKET_NAME = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class, "core.couchbase.dao.{domain}.{name}.bucket", (String) null);

    @ConfigPropertyDoc(
            name="core.couchbase.dao.{domain}.{name}.entity.class",
            descr = "defines the attached couchbase class for given domain/name",
            examples = {"com.project.business.model.Party","com.project.business.model.BillingAccount"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_DAO_ENTITY_CLASS_NAME = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class, "core.couchbase.dao.{domain}.{name}.entity.class", (String) null);

    @ConfigPropertyDoc(
            name="core.couchbase.dao.{domain}.{name}.class",
            descr = "defines the attached dao class for given domain/name",
            examples = {"com.project.business.dao.PartyDao","com.project.business.dao.BillingAccount"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_DAO_CLASS_NAME = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class, "core.couchbase.dao.{domain}.{name}.class", (String) null);

    @ConfigPropertyDoc(
            name="core.couchbase.dao.{domain}.{name}.{flavor}.bucket",
            descr = "defines the attached couchbase bucket name for given domain/name",
            defaultValue = "@Ref{core.couchbase.dao.{domain}.{name}.bucket,dao}",
            examples = {"default bucket"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_DAO_BUCKET_NAME_FOR_FLAVOR = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class, "core.couchbase.dao.{domain}.{name}.{flavor}.bucket", COUCHBASE_DAO_BUCKET_NAME);


    @ConfigPropertyDoc(
            name="core.couchbase.dao.{domain}.{name}.{flavor}.entity.class",
            descr = "defines the attached couchbase class for given domain/name",
            defaultValue = "@Ref{core.couchbase.dao.{domain}.{name}.entity.class,dao}",
            examples = {"com.project.business.model.Party","com.project.business.model.BillingAccount"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_DAO_ENTITY_CLASS_NAME_FOR_FLAVOR = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class, "core.couchbase.dao.{domain}.{name}.{flavor}.entity.class", COUCHBASE_DAO_ENTITY_CLASS_NAME);

    @ConfigPropertyDoc(
            name="core.couchbase.dao.{domain}.{name}.{flavor}.class",
            descr = "defines the attached dao class for given domain/name",
            defaultValue = "@Ref{core.couchbase.dao.{domain}.{name}.class,dao}",
            examples = {"com.project.business.dao.PartyDao","com.project.business.dao.BillingAccount"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> COUCHBASE_DAO_CLASS_NAME_FOR_FLAVOR = ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class, "core.couchbase.dao.{domain}.{name}.{flavor}.class", COUCHBASE_DAO_CLASS_NAME);

}

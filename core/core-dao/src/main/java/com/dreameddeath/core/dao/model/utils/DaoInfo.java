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

package com.dreameddeath.core.dao.model.utils;

import com.dreameddeath.core.model.entity.model.EntityDef;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 22/10/2015.
 */
public class DaoInfo {
    @JsonProperty("className")
    private String className;
    @JsonProperty("entityDef")
    private EntityDef entityDef;
    @JsonProperty("parentDaoClassName")
    private String parentDaoClassName=null;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public EntityDef getEntityDef() {
        return entityDef;
    }

    public void setEntityDef(EntityDef entityDef) {
        this.entityDef = entityDef;
    }

    public String getParentDaoClassName() {
        return parentDaoClassName;
    }

    public void setParentDaoClassName(String parentDaoClassName) {
        this.parentDaoClassName = parentDaoClassName;
    }
}

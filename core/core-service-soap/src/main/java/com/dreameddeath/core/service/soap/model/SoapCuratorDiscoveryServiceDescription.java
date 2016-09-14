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

package com.dreameddeath.core.service.soap.model;

import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.soap.SoapServiceTypeHelper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 * Created by Christophe Jeunesse on 05/09/2016.
 */
public class SoapCuratorDiscoveryServiceDescription extends CuratorDiscoveryServiceDescription<String> {
    @JsonProperty
    private String className;
    @Override
    public String getServiceType() {
        return SoapServiceTypeHelper.SERVICE_TYPE;
    }

    @Override
    public void setServiceType(String serviceType) {
        Preconditions.checkArgument(SoapServiceTypeHelper.SERVICE_TYPE.equals(SoapServiceTypeHelper.SERVICE_TYPE),"Wrong service type %s",serviceType);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}

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

package com.dreameddeath.core.service.model.rest;

import com.dreameddeath.core.service.model.common.ServiceInfoVersionDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.models.Swagger;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public class RestServiceInfoVersionDescription extends ServiceInfoVersionDescription<Swagger> {

    @JsonIgnore
    public Swagger getSwagger() {
        return getSpec();
    }
    @JsonIgnore
    public void setSwagger(Swagger swagger) {
        setSpec(swagger);
    }

}

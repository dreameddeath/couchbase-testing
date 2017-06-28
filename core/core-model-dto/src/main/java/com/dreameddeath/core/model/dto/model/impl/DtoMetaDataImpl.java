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

package com.dreameddeath.core.model.dto.model.impl;

import com.dreameddeath.core.model.dto.model.ICouchbaseDocumentDto;
import com.dreameddeath.core.model.entity.model.EntityModelId;

/**
 * Created by christophe jeunesse on 02/02/2017.
 */
public class DtoMetaDataImpl implements ICouchbaseDocumentDto.IDtoMetaData {
    private final EntityModelId modelId;
    private final String key;
    private final String cas;

    public DtoMetaDataImpl(String domain, String name, String version){
        this(EntityModelId.build(domain, name, version),null,null);
    }

    public DtoMetaDataImpl(EntityModelId modelId, String key, String cas) {
        this.modelId = modelId;
        this.key = key;
        this.cas = cas;
    }

    @Override
    public String domain() {
        return modelId.getDomain();
    }

    @Override
    public String name() {
        return modelId.getName();
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String version() {
        return modelId.getEntityVersion().toString();
    }

    @Override
    public String cas() {
        return cas;
    }
}

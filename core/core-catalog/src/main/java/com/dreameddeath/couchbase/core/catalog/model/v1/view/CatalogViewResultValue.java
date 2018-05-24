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

package com.dreameddeath.couchbase.core.catalog.model.v1.view;

import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 03/01/2018.
 */
public class CatalogViewResultValue {
    @JsonProperty
    public final Catalog.State state;
    @JsonProperty
    public final Version version;

    @JsonCreator
    public CatalogViewResultValue(@JsonProperty Catalog.State state, @JsonProperty Version version) {
        this.state = state;
        this.version = version;
    }
}

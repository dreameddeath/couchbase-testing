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

package com.dreameddeath.core.model.dto.model.manager;

import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by CEAJ8230 on 03/06/2017.
 */
public class DtoModelDef {
    @JsonProperty("class")
    private final String className;
    @JsonProperty("origClass")
    private final String origClassName;
    @JsonProperty("mode")
    private final DtoInOutMode mode;
    @JsonProperty("type")
    private final String type;
    @JsonProperty("version")
    private final String version;

    @JsonCreator
    public DtoModelDef(
            @JsonProperty("class") String className,
            @JsonProperty("origClass") String origClassName,
            @JsonProperty("mode") DtoInOutMode mode,
            @JsonProperty("type") String type,
            @JsonProperty("version") String version)
    {
        this.className = className;
        this.origClassName = origClassName;
        this.mode = mode;
        this.type = type;
        this.version = version;
    }

    @JsonGetter("class")
    public String getClassName() {
        return className;
    }

    @JsonGetter("mode")
    public DtoInOutMode getMode() {
        return mode;
    }

    @JsonGetter("type")
    public String getType() {
        return type;
    }

    @JsonGetter("version")
    public String getVersion() {
        return version;
    }

    @JsonGetter("origClass")
    public String getOrigClassName() {
        return origClassName;
    }
}

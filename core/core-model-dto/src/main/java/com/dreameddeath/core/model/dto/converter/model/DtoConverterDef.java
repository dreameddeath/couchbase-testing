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

package com.dreameddeath.core.model.dto.converter.model;

import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by ceaj8230 on 07/03/2017.
 */
public class DtoConverterDef {
    @JsonProperty("mode")
    private DtoInOutMode mode;
    @JsonProperty("type")
    private String type;

    @JsonProperty("entity")
    private EntityModelId entityModelId;
    @JsonProperty("entityClassName")
    private String entityClassName;
    @JsonProperty("converter")
    private String converterClass;
    @JsonProperty("converterVersion")
    private String converterVersion;
    @JsonProperty("inputClass")
    private String inputClass;
    @JsonProperty("inputVersion")
    private String inputVersion;
    @JsonProperty("outputClass")
    private String outputClass;
    @JsonProperty("outputVersion")
    private String outputVersion;

    public DtoInOutMode getMode() {
        return mode;
    }

    public void setMode(DtoInOutMode mode) {
        this.mode = mode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConverterClass() {
        return converterClass;
    }

    public void setConverterClass(String converterClass) {
        this.converterClass = converterClass;
    }

    public String getConverterVersion() {
        return converterVersion;
    }

    public void setConverterVersion(String converterVersion) {
        this.converterVersion = converterVersion;
    }

    public EntityModelId getEntityModelId() {
        return entityModelId;
    }

    public void setEntityModelId(EntityModelId entityModelId) {
        this.entityModelId = entityModelId;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public String getInputClass() {
        return inputClass;
    }

    public void setInputClass(String inputClass) {
        this.inputClass = inputClass;
    }

    public String getInputVersion() {
        return inputVersion;
    }

    public void setInputVersion(String inputVersion) {
        this.inputVersion = inputVersion;
    }

    public String getOutputClass() {
        return outputClass;
    }

    public void setOutputClass(String outputClass) {
        this.outputClass = outputClass;
    }

    public String getOutputVersion() {
        return outputVersion;
    }

    public void setOutputVersion(String outputVersion) {
        this.outputVersion = outputVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DtoConverterDef that = (DtoConverterDef) o;

        if (!entityModelId.equals(that.entityModelId)) return false;
        if (!converterClass.equals(that.converterClass)) return false;
        if (!converterVersion.equals(that.converterVersion)) return false;
        if (inputClass != null ? !inputClass.equals(that.inputClass) : that.inputClass != null) return false;
        if (inputVersion != null ? !inputVersion.equals(that.inputVersion) : that.inputVersion != null) return false;
        if (outputClass != null ? !outputClass.equals(that.outputClass) : that.outputClass != null) return false;
        return outputVersion != null ? outputVersion.equals(that.outputVersion) : that.outputVersion == null;
    }

    @Override
    public int hashCode() {
        int result = entityModelId.hashCode();
        result = 31 * result + converterClass.hashCode();
        result = 31 * result + converterVersion.hashCode();
        result = 31 * result + (inputClass != null ? inputClass.hashCode() : 0);
        result = 31 * result + (inputVersion != null ? inputVersion.hashCode() : 0);
        result = 31 * result + (outputClass != null ? outputClass.hashCode() : 0);
        result = 31 * result + (outputVersion != null ? outputVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DtoConverterDef{" +
                "entityModelId=" + entityModelId +
                ", converterClass='" + converterClass + '\'' +
                ", converterVersion='" + converterVersion + '\'' +
                ", inputClass='" + inputClass + '\'' +
                ", inputVersion='" + inputVersion + '\'' +
                ", outputClass='" + outputClass + '\'' +
                ", outputVersion='" + outputVersion + '\'' +
                '}';
    }
}

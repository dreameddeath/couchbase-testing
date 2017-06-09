package com.dreameddeath.core.model.dto.model.manager;

import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by CEAJ8230 on 03/06/2017.
 */
public class DtoModelDef {
    @JsonProperty("class")
    private final String className;
    @JsonProperty("entity")
    private final EntityModelId entityModelId;
    @JsonProperty("mode")
    private final DtoInOutMode mode;
    @JsonProperty("type")
    private final String type;
    @JsonProperty("version")
    private final String version;

    @JsonCreator
    public DtoModelDef(
            @JsonProperty("class") String className,
            @JsonProperty("entity") EntityModelId entityModelId,
            @JsonProperty("mode") DtoInOutMode mode,
            @JsonProperty("type") String type,
            @JsonProperty("version") String version)
    {
        this.className = className;
        this.entityModelId = entityModelId;
        this.mode = mode;
        this.type = type;
        this.version = version;
    }

    @JsonGetter("class")
    public String getClassName() {
        return className;
    }

    @JsonGetter("entity")
    public EntityModelId getEntityModelId() {
        return entityModelId;
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
}

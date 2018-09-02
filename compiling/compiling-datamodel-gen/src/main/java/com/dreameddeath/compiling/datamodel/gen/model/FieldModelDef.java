package com.dreameddeath.compiling.datamodel.gen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

public class FieldModelDef {
    public String name;
    public String dbName;
    public String since;
    public String type;
    public Set<Flag> flags=new HashSet<>();
    @JsonProperty("default")
    public String defaultStr;
    public String description;

    public enum Flag{
        IMMUTABLE,
        NOTNULL;
        @JsonCreator
        public static Flag fromString(String key) {
            return key == null
                    ? null
                    : Flag.valueOf(key.toUpperCase());
        }
    }
}

package com.dreameddeath.compiling.datamodel.gen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelDef {
    public String fullFilename;
    public String relativeFilename;
    @JsonProperty("package")
    public String packageName;
    public String name;
    public String version;
    public String dbName;
    public String parent;
    public Set<Flag> flags=new HashSet<>();

    public List<FieldModelDef> fields = new ArrayList<>();

    public enum Flag{
        ABSTRACT,
        ELEMENT;

        @JsonCreator
        public static Flag fromString(String key) {
            return key == null
                    ? null
                    : Flag.valueOf(key.toUpperCase());
        }
    }
}

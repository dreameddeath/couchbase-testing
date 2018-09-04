package com.dreameddeath.core.model.v2.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class EffectiveDomainMetaInfo {
    public final String domain;

    @JsonCreator
    public EffectiveDomainMetaInfo(String domain) {
        this.domain = domain;
    }
    @JsonValue
    public final String getDomain(){
        return this.domain;
    }
}

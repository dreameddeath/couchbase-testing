package com.dreameddeath.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by CEAJ8230 on 17/01/2015.
 */
public class ServiceDescription {
    @JsonProperty("version")
    private String _version;
    @JsonProperty("state")
    private String _state;
    @JsonProperty("swagger")
    private String _swagger;

    public String getVersion() {
        return _version;
    }

    public void setVersion(String version) {
        _version = version;
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    public String getSwagger() {
        return _swagger;
    }

    public void setSwagger(String swagger) {
        _swagger = swagger;
    }
}

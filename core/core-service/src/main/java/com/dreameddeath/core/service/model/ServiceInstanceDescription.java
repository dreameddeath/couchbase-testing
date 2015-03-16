package com.dreameddeath.core.service.model;

import com.dreameddeath.core.service.utils.ServiceJacksonObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.curator.x.discovery.ServiceInstance;

/**
 * Created by CEAJ8230 on 04/03/2015.
 */
public class ServiceInstanceDescription{
    private static ServiceJacksonObjectMapper _OBJECT_MAPPER = ServiceJacksonObjectMapper.getInstance();

    @JsonProperty("name")
    private String _name;
    @JsonProperty("address")
    private String _address;
    @JsonProperty("version")
    private String _version;
    @JsonProperty("port")
    private Integer _port;
    @JsonProperty("uid")
    private String _uid;
    @JsonProperty("swagger")
    private JsonNode _swagger;
    @JsonProperty("state")
    private String _state;

    public ServiceInstanceDescription(ServiceInstance<ServiceDescription> instance){
        _name =instance.getName();
        _address = instance.getAddress();
        _uid = instance.getId();
        try {
            _swagger = _OBJECT_MAPPER.readTree(instance.getPayload().getSwagger());
        }catch(Exception e){
            //TODO throw an error
        }
        _version = instance.getPayload().getVersion();
        _state = instance.getPayload().getState();
        _port=instance.getPort();
    }

    public ServiceInstanceDescription(){}

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String address) {
        _address = address;
    }

    public String getVersion() {
        return _version;
    }

    public void setVersion(String version) {
        _version = version;
    }

    public Integer getPort() {
        return _port;
    }

    public void setPort(Integer port) {
        _port = port;
    }

    public String getUid() {
        return _uid;
    }

    public void setUid(String uid) {
        _uid = uid;
    }

    public JsonNode getSwagger() {
        return _swagger;
    }

    public void setSwagger(JsonNode swagger) {
        _swagger = swagger;
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }
}


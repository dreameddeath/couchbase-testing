package com.dreameddeath.couchbase.core.process.remote.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 21/05/2016.
 */
public class AlreadyExistingJob {
    @JsonProperty
    public String key;
    @JsonProperty
    public String uid;
    @JsonProperty
    public String requestUid;
    @JsonProperty
    public String jobModelId;
}

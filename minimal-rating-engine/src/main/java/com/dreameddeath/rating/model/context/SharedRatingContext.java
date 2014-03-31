package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.dreameddeath.common.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;
import com.dreameddeath.common.storage.CouchbaseDocument;

@JsonInclude(Include.NON_EMPTY)
public final class SharedRatingContext extends AbstractRatingContext{
    private static GenericJacksonTranscoder<SharedRatingContext> _tc = new GenericJacksonTranscoder<SharedRatingContext>(SharedRatingContext.class);
    @JsonIgnore
    public  Transcoder<SharedRatingContext> getTranscoder(){
        return _tc;
    }

}
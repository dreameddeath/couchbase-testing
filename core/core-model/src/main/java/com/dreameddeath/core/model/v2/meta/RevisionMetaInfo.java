package com.dreameddeath.core.model.v2.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.Optional;

public final class RevisionMetaInfo {
    private final Long number;
    private final DateTime lastModification;

    @JsonCreator
    public RevisionMetaInfo(@JsonProperty("number") long rev,@JsonProperty("lastMod") DateTime lastModification){
        this.number = rev;
        this.lastModification = lastModification;
    }

    public RevisionMetaInfo(){
        this.number = null;
        this.lastModification =null;
    }

    @JsonGetter("number")
    public Optional<Long> revision(){
        return Optional.ofNullable(this.number);
    }

    @JsonGetter("lastMod")
    public Optional<DateTime> lastModificationDate(){
        return Optional.ofNullable(this.lastModification);
    }

    public RevisionMetaInfo updateRevision(DateTime newUpdateDate){
        return new RevisionMetaInfo(this.number !=null?this.number +1:0L, newUpdateDate);
    }
}

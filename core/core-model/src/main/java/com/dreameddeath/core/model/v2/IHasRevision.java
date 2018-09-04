package com.dreameddeath.core.model.v2;

import com.dreameddeath.core.model.v2.meta.RevisionMetaInfo;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.joda.time.DateTime;

public interface IHasRevision {
    @JsonGetter("@rev")
    RevisionMetaInfo getRevisionInfo();
    <T extends Builder> Builder<T> toMutable();
    interface Builder<T extends Builder>{
        @JsonSetter("@rev")
        T withRevisionInfo(RevisionMetaInfo revisionInfo);

        T updateRevisionDate(DateTime newDate);

        IHasRevision create();
    }
}

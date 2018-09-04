package com.dreameddeath.core.model.v2;

import com.dreameddeath.core.model.v2.meta.UniqueKeyMetaInfo;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public interface IHasUniqueKeys {
    @JsonGetter("@uniqKeys")
    UniqueKeyMetaInfo getUniqueKeys();

    <T extends Builder> Builder<T> toMutable();

    interface Builder<T extends Builder>{
        @JsonSetter("@uniqKeys")
        T withUniqueKeys(UniqueKeyMetaInfo uniqKeys);

        T addNewUniqueKey(String uniqueKey);

        IHasRevision create();
    }
}

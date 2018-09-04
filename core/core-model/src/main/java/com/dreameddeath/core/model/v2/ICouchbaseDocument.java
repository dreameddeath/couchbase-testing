package com.dreameddeath.core.model.v2;

import com.dreameddeath.core.model.v2.meta.CouchbaseMetaInfo;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,fieldVisibility= JsonAutoDetect.Visibility.NONE,isGetterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,creatorVisibility = JsonAutoDetect.Visibility.NONE)
public interface ICouchbaseDocument{
    CouchbaseMetaInfo getCouchbaseMetaInfo();

    <T extends Builder> Builder<T> toMutable();
    interface Builder<T extends Builder>{
        Builder<T> withCouchbaseMetaInfo(CouchbaseMetaInfo metaInfo);

        ICouchbaseDocument create();
    }
}

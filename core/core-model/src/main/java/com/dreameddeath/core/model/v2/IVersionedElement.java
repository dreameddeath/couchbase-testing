package com.dreameddeath.core.model.v2;

import com.dreameddeath.core.model.v2.meta.VersionMetaInfo;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, property="@t",visible = true)
//@JsonTypeIdResolver(VersionedTypeIdResolver.class)
public interface IVersionedElement {
    @JsonGetter("@t")
    VersionMetaInfo getVersionInfo();

    <T extends Builder> Builder<T> toMutable();

    interface Builder<T extends Builder>{
        @JsonSetter("@t")
        T withVersionInfo(VersionMetaInfo info);

        default T withVersionInfo(Class<?> info){
            return withVersionInfo(new VersionMetaInfo(info));
        }

        VersionMetaInfo create();
    }
}

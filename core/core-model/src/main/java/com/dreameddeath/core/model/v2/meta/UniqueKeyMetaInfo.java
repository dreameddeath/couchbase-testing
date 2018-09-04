package com.dreameddeath.core.model.v2.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class UniqueKeyMetaInfo {
    private final Set<String> inDbUniqueKeys;
    private final Set<String> newUniqueKeys;

    @JsonCreator
    public UniqueKeyMetaInfo(Set<String> inDbUniqueKeys){
        this.inDbUniqueKeys = Collections.unmodifiableSet(inDbUniqueKeys);
        this.newUniqueKeys = Collections.emptySet();
    }


    public UniqueKeyMetaInfo(){
        this.inDbUniqueKeys = Collections.emptySet();
        this.newUniqueKeys = Collections.emptySet();
    }

    private UniqueKeyMetaInfo(Set<String> inDbUniqueKeys,Set<String> newUniqueKeys){
        this.inDbUniqueKeys = inDbUniqueKeys;
        this.newUniqueKeys = newUniqueKeys;
    }

    public boolean isKeyInDb(String key){
        return this.inDbUniqueKeys.contains(key);
    }

    public Set<String> getInDbUniqueKeys() {
        return inDbUniqueKeys;
    }

    @JsonValue
    public Set<String> getNewUniqueKeys() {
        return newUniqueKeys;
    }

    public Set<String> getRemovedUniqueKeys(){
        Set<String> removed=new TreeSet<>(this.inDbUniqueKeys);
        removed.removeAll(this.inDbUniqueKeys);
        return removed;
    }


    public UniqueKeyMetaInfo addUniqueKey(String key){
        Set<String> newSet = new TreeSet<>(newUniqueKeys);
        newSet.add(key);
        return new UniqueKeyMetaInfo(this.inDbUniqueKeys,Collections.unmodifiableSet(newSet));
    }


}

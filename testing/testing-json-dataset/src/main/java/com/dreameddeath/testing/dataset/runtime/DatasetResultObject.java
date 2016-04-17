package com.dreameddeath.testing.dataset.runtime;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetResultObject {
    private Map<String,DatasetResultValue> values=new HashMap<>();


    public DatasetResultValue get(String name){
        return values.get(name);
    }

    public DatasetResultValue getOrCreate(String name){
        return values.computeIfAbsent(name,newName->new DatasetResultValue());
    }


    public Collection<DatasetResultValue> getAll(){
        return values.values();
    }

    @JsonValue
    public Map<String,DatasetResultValue> getValuesMap(){
        return Collections.unmodifiableMap(values);
    }
}

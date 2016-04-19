package com.dreameddeath.testing.dataset.runtime.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetResultObject implements Cloneable{
    private final Map<String,DatasetResultValue> values=new TreeMap<>();

    public DatasetResultValue get(String name){
        return values.get(name);
    }

    public DatasetResultValue getOrCreate(String name){
        return values.computeIfAbsent(name,newName->new DatasetResultValue());
    }


    public List<DatasetResultValue> getAll(){
        return new ArrayList<>(values.values());
    }

    @JsonValue
    public Map<String,DatasetResultValue> getValuesMap(){
        return Collections.unmodifiableMap(values);
    }

    @Override
    public DatasetResultObject clone(){
        DatasetResultObject result=new DatasetResultObject();
        for(Map.Entry<String,DatasetResultValue> entry:this.values.entrySet()){
            result.values.put(entry.getKey(),entry.getValue().clone());
        }
        return result;
    }
}

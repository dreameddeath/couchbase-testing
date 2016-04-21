package com.dreameddeath.testing.dataset.runtime.model;

import com.dreameddeath.testing.dataset.DatasetManager;
import com.dreameddeath.testing.dataset.converter.IDatasetResultConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class DatasetResult {
    private final Map<String,DatasetResultValue> resultingDatasetMap = new HashMap<>();
    private final DatasetManager manager;

    public DatasetResult(DatasetManager manager){
        this.manager = manager;
    }

    public void add(String name, DatasetResultValue value){
        resultingDatasetMap.putIfAbsent(name,value);
    }

    public boolean contains(String name){
        return resultingDatasetMap.containsKey(name);
    }

    public DatasetResultValue get(String name){
        return resultingDatasetMap.get(name);
    }

    public String buildAnonymousName(){
        return "anonymous_dataset_#"+resultingDatasetMap.values().size();
    }

    public <T> T get(Class<T> tClass,String name){
        DatasetResultValue value = get(name);

        if(value==null){
            return null;
        }
        else{
            IDatasetResultConverter<T> mapper = manager.getMapperForClass(tClass);
            if(mapper!=null){
                return mapper.mapResult(tClass,value);
            }
            else{
                throw new RuntimeException("Cannot find mapper for class <"+tClass.getName());
            }
        }

    }


}

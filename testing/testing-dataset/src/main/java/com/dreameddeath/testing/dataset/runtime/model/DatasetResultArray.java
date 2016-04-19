package com.dreameddeath.testing.dataset.runtime.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetResultArray implements Cloneable{
    private ArrayList<DatasetResultValue> values=new ArrayList<>();

    public DatasetResultValue getOrCreate(Integer exact) {
        expandToPos(exact);
        return get(exact);
    }

    public DatasetResultValue get(Integer exact){
        return values.get(exact);
    }

    public List<DatasetResultValue> getOrCreate(Integer min,Integer max){
        expandToPos(max);
        return get(min,max);
    }

    public List<DatasetResultValue> get(Integer min,Integer max){
        int effectiveMax = (max>=values.size())?values.size():max+1;
        return values.subList(min,effectiveMax);
    }

    private void expandToSize(int size){
        values.ensureCapacity(size);
        while(values.size()<size){
            values.add(new DatasetResultValue());
        }
    }


    private void expandToPos(int pos){
        expandToSize(pos+1);
    }

    public void add(DatasetResultValue value){
        values.add(value);
    }

    @JsonValue
    public List<DatasetResultValue> getValues(){
        return Collections.unmodifiableList(values);
    }

    @Override
    public DatasetResultArray clone(){
        DatasetResultArray result = new DatasetResultArray();
        result.values.ensureCapacity(this.values.size());
        for(DatasetResultValue value:this.values){
            result.values.add(value.clone());
        }
        return result;
    }
}

package com.dreameddeath.testing.dataset.runtime;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetResultArray {
    private ArrayList<DatasetResultValue> values=new ArrayList<>();
    public DatasetResultValue getOrCreate(Integer exact) {
        expandToPos(exact);
        return values.get(0);
    }

    public List<DatasetResultValue> getOrCreate(Integer min,Integer max){
        expandToPos(max);
        return values.subList(min,max+1);
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
}

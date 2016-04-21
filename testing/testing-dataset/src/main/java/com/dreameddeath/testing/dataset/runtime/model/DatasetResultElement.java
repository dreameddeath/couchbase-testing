package com.dreameddeath.testing.dataset.runtime.model;

import com.dreameddeath.testing.dataset.converter.IDatasetResultConverter;
import com.dreameddeath.testing.dataset.model.DatasetElement;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class DatasetResultElement {
    private Map<String,Object> params;
    private String name;
    private DatasetElement validatorElement;
    private DatasetResultValue value;


    public boolean validate(){
        //validate(value,datasetName,dataSetElementName,Collections.emptyMap())
        return false;
    }

    public DatasetResultValue get(){
        return value;
    }

    public <T> T get(Class<T> tClass){
        if(value==null){
            return null;
        }
        else{
            IDatasetResultConverter<T> mapper = validatorElement.getDataset().getManager().getMapperForClass(tClass);
            if(mapper!=null){
                return mapper.mapResult(tClass,value);
            }
            else{
                throw new RuntimeException("Cannot find mapper for class <"+tClass.getName());
            }
        }

    }
}

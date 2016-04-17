package com.dreameddeath.testing.dataset.validate;

import com.dreameddeath.testing.dataset.model.DatasetValue;
import com.dreameddeath.testing.dataset.runtime.DatasetResultValue;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetValidator {
    public boolean validate(DatasetResultValue source, DatasetValue ref){
        if(source.getType()!=ref.getType()){
            return false;
        }
        switch (source.getType()){
            case ARRAY:

                break;
            case OBJECT:

                break;
            default:
                return (source.getContent()!=null) && source.getContent().equals(ref.getContent());
        }
        return false;
    }
}

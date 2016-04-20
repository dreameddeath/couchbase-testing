package com.dreameddeath.testing.dataset.json.mapper;

import com.dreameddeath.testing.dataset.converter.IDatasetResultConverter;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class BaseJsonConverter implements IDatasetResultConverter {
    @Override
    public boolean canMap(Class clazz) {
        return true;
    }

    @Override
    public DatasetResultValue mapObject(Object src) {
        return null;
    }

    @Override
    public Object mapResult(DatasetResultValue value) {
        return null;
    }
}

package com.dreameddeath.testing.dataset.converter;

import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public interface IDatasetResultConverter<T> {
    boolean canMap(Class<?> clazz);
    DatasetResultValue mapObject(T src);
    <TSUB extends T> TSUB mapResult(Class<TSUB> clazz,DatasetResultValue value);
}

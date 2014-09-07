package com.dreameddeath.core.model.property;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public interface MapDefaultValueBuilder<T> {
    public T build(MapProperty<?,T> map);
}

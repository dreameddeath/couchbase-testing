package com.dreameddeath.common.model.property;

public interface Property<T>  {
    public  T get();
    public boolean set(T value);
}

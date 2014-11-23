package com.dreameddeath.core.model.property;

public interface Property<T>  {
    public  T get();
    public boolean set(T value);
}

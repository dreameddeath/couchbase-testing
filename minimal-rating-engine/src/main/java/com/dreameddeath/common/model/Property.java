package com.dreameddeath.common.model;

public interface Property<T>  {
    public  T get();
    public boolean set(T value);
}

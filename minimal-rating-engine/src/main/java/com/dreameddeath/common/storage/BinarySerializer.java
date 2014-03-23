package com.dreameddeath.common.storage;


public interface BinarySerializer<T> {
    public T deserialize(byte[] input);
    public byte[] serialize(T input);
}
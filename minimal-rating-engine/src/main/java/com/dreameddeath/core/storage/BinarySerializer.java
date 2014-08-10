package com.dreameddeath.core.storage;

/**
* Interface to be implemented for a binary (byte array) Serializer/Deserializer Class
*/
public interface BinarySerializer<T> {
    public T deserialize(byte[] input);
    public byte[] serialize(T input);
}
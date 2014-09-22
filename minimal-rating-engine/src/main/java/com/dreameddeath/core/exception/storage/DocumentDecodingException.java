package com.dreameddeath.core.exception.storage;

import net.spy.memcached.CachedData;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by ceaj8230 on 16/09/2014.
 */
public class DocumentDecodingException extends RuntimeException{
    private CachedData _data;
    public DocumentDecodingException(String message, CachedData data,Throwable e){
        super(message,e);
        _data = data;
    }

    public String getMessage(){
        StringBuilder result = new StringBuilder(super.getMessage());
        result.append(" with data ");
        try{
            result.append(new String(_data.getData(),"UTF-8"));
        }
        catch(UnsupportedEncodingException e){
            result.append(_data.getData().toString());
        }
        return result.toString();
    }
}

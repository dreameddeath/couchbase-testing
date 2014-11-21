package com.dreameddeath.core.exception.storage;

import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by ceaj8230 on 16/09/2014.
 */
public class DocumentDecodingException extends RuntimeException{
    private ByteBuf _data;
    public DocumentDecodingException(String message, ByteBuf data,Throwable e){
        super(message,e);
        _data = data;
    }

    public String getMessage(){
        StringBuilder result = new StringBuilder(super.getMessage());
        result.append(" with data ");
        try{
            result.append(new String(_data.array(),"UTF-8"));
        }
        catch(UnsupportedEncodingException e){
            result.append(Arrays.toString(_data.array()));
        }
        return result.toString();
    }
}

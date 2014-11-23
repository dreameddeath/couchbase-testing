package com.dreameddeath.core.exception.storage;

/**
 * Created by CEAJ8230 on 21/09/2014.
 */
public class DocumentAccessException extends StorageException {
    private String _key;
    public DocumentAccessException(String key){_key=key;}
    public DocumentAccessException(String key,Throwable e){
        super(e);
        _key=key;
    }
    public DocumentAccessException(String key,String message, Throwable e){
        super(message,e);
        _key=key;
    }
    public DocumentAccessException(String key,String message){
        super(message);
        _key = key;
    }

    public String getKey(){ return _key;}

    @Override
    public String getMessage(){
        StringBuilder builder=new StringBuilder(super.getMessage());
        return builder.append("\nThe key was <").append(_key).append(">").toString();
    }
}

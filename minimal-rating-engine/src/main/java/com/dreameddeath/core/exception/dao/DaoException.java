package com.dreameddeath.core.exception.dao;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class DaoException extends Exception {
    public DaoException(Throwable e){
        super(e);
    }

    public DaoException(String message, Throwable e){
        super(message,e);
    }

    public DaoException(String message){
        super(message);
    }
}

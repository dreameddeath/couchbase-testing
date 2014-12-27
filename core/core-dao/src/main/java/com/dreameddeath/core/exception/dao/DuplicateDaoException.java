package com.dreameddeath.core.exception.dao;

/**
 * Created by ceaj8230 on 18/12/2014.
 */
public class DuplicateDaoException extends RuntimeException {
    public DuplicateDaoException(String message){super(message);}
    public DuplicateDaoException(String message,Throwable e){super(message,e);}
}

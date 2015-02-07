package com.dreameddeath.core.exception.curator;

/**
 * Created by CEAJ8230 on 06/02/2015.
 */
public class BadConnectionStringException extends Exception{
    private String _connectionString;

    public BadConnectionStringException(String connectionString){
        super("The connection string was inconsistent <"+connectionString+">");
        _connectionString = connectionString;
    }
}

package com.dreameddeath.core.exception.validation;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class ValidationException extends Exception {
    public ValidationException(Throwable e){ super(e);}
    public ValidationException(String message,Throwable e){ super(message,e);}
    public ValidationException(String message){ super(message);}
    public ValidationException(){ super();}
}

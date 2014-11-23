package com.dreameddeath.core.user;

/**
 * Created by ceaj8230 on 10/09/2014.
 */
public interface User {
    public String getUserId();
    public Boolean hasRight(String namme);
    public String getProperty(String name);
}

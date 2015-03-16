package com.dreameddeath.core.service.context;

/**
 * Created by CEAJ8230 on 05/03/2015.
 */
public interface IGlobalContext {
    public ICallerContext caller();
    public IExternalCallerContext external();
    public IUserContext user();
}

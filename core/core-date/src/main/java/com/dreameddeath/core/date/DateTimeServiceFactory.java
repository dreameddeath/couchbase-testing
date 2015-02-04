package com.dreameddeath.core.date;

/**
 * Created by ceaj8230 on 03/11/2014.
 */
public class DateTimeServiceFactory {
    public IDateTimeService getService(){
        return new DateTimeServiceImpl();
    }
}

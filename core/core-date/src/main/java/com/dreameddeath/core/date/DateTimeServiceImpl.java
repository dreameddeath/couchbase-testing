package com.dreameddeath.core.date;

import org.joda.time.DateTime;

/**
 * Created by ceaj8230 on 03/11/2014.
 */
public class DateTimeServiceImpl implements IDateTimeService {

    public DateTime getCurrentDate(){
        return DateTime.now();
    }
    public DateTime now(){return getCurrentDate();}
}

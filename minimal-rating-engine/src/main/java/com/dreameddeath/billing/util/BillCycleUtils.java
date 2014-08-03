package com.dreameddeath.billing.util;

import org.joda.time.DateTime;

/**
 * Created by CEAJ8230 on 03/08/2014.
 */
public class BillCycleUtils {
    public static DateTime CalcCycleEndDate(DateTime startDate, Integer bDom,Integer cycleLengh){
        DateTime endDate = startDate.withTime(0,0,0,0).plusMonths(cycleLengh);

        while(endDate.dayOfMonth().get()!=bDom){
            endDate = endDate.minusDays(1);
        }
        return endDate;
    }
}

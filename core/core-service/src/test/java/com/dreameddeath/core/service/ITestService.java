package com.dreameddeath.core.service;

import com.dreameddeath.core.service.context.IGlobalContext;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * Created by CEAJ8230 on 05/03/2015.
 */
public interface ITestService {

    public Observable<Result> runWithRes(IGlobalContext ctxt,Input input);

    public static class Result{
        String result;
        String id;
        String rootId;
        DateTime plusOneMonth;
    }

    public static class Input{
        String id;
        String rootId;
        DateTime otherField;
    }
}

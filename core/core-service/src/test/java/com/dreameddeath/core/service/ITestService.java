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
        public String result;
        public String id;
        public String rootId;
        public DateTime plusOneMonth;
    }

    public static class Input{
        public String id;
        public String rootId;
        public DateTime otherField;
    }
}

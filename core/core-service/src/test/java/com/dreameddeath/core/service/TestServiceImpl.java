package com.dreameddeath.core.service;

import com.dreameddeath.core.service.annotation.ExposeMethod;
import com.dreameddeath.core.service.annotation.ExposeService;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.context.IGlobalContext;
import rx.Observable;

import javax.ws.rs.HttpMethod;

/**
 * Created by CEAJ8230 on 05/03/2015.
 */
@ExposeService(path="/TestService")
@ServiceDef(name="testService",version="1.0",status = "stable")
public class TestServiceImpl implements ITestService {

    @Override @ExposeMethod(method=HttpMethod.POST,path="/toto/:input.rootId/tuto/:input.id")
    public Observable<Result> runWithRes(IGlobalContext ctxt, Input input) {
        Result res = new Result();
        res.result = "HTTP 200";
        res.id = input.id;
        res.rootId =  input.rootId;
        res.plusOneMonth = input.otherField.plusMonths(1);
        return Observable.just(res);
    }
}

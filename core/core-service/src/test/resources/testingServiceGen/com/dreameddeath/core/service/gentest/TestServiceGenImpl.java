/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.service.gentest;

import com.dreameddeath.core.service.annotation.BodyInfo;
import com.dreameddeath.core.service.annotation.ExposeMethod;
import com.dreameddeath.core.service.annotation.ExposeService;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.context.IGlobalContext;
import org.joda.time.DateTime;
import rx.Observable;
import com.dreameddeath.core.service.*;

import javax.ws.rs.HttpMethod;

/**
 * Created by CEAJ8230 on 05/03/2015.
 */
@ExposeService(path="/TestServiceGen",name = "testServiceGen",version = "1.0",status = VersionStatus.STABLE)
public class TestServiceGenImpl implements ITestService {
    @Override
    @ExposeMethod(
            method=HttpMethod.POST,
            path="/toto/:rootId=input.rootId/tuto/:id=input.id",
            status = VersionStatus.STABLE
    )
    @BodyInfo(paramName = "input")
    public Observable<Result> runWithRes(IGlobalContext ctxt, Input input) {
        Result res = new Result();
        res.result = "HTTP 200";
        res.id = input.id+" gen";
        res.rootId =  input.rootId+" gen";
        res.plusOneMonth = input.otherField.plusMonths(1);
        return Observable.just(res);
    }


    @Override
    @ExposeMethod(
            method=HttpMethod.GET,
            path="/toto/:rootId/tuto/:id=id",
            status = VersionStatus.STABLE
    )
    public Observable<Result> getWithRes(String rootId, String id) {
        Result res = new Result();
        res.result = "HTTP 200";
        res.id = id+" gen";
        res.rootId =  rootId+" gen";
        res.plusOneMonth = DateTime.now().plusMonths(1);
        return Observable.just(res);
    }

    @Override
    @ExposeMethod(
            method=HttpMethod.GET,
            path="/toto/:rootId?id=id",
            status = VersionStatus.STABLE
    )
    public Observable<Result> putWithQuery(String rootId, String id) {
        Result res = new Result();
        res.result = "HTTP 200";
        res.id = id+" putgen";
        res.rootId =  rootId+" putgen";
        res.plusOneMonth = DateTime.now().plusMonths(1);
        return Observable.just(res);
    }


}

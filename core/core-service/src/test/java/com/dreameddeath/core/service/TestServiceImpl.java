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

package com.dreameddeath.core.service;

import com.dreameddeath.core.service.annotation.ExposeMethod;
import com.dreameddeath.core.service.annotation.ExposeService;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.service.context.IGlobalContext;
import rx.Observable;

import javax.ws.rs.HttpMethod;

/**
 * Created by CEAJ8230 on 05/03/2015.
 */
@ExposeService(path="/TestService",name = "testService",version = "1.0",status = VersionStatus.STABLE)
public class TestServiceImpl implements ITestService {

    @Override @ExposeMethod(method=HttpMethod.POST,path="/toto/:input.rootId/tuto/:input.id",status = VersionStatus.STABLE)
    public Observable<Result> runWithRes(IGlobalContext ctxt, Input input) {
        Result res = new Result();
        res.result = "HTTP 200";
        res.id = input.id;
        res.rootId =  input.rootId;
        res.plusOneMonth = input.otherField.plusMonths(1);
        return Observable.just(res);
    }
}

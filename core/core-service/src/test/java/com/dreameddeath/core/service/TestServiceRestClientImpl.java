/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.service;


import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.dreameddeath.core.service.client.rest.rxjava.RxJavaWebTarget;
import com.dreameddeath.core.service.swagger.TestingDocument;
import com.dreameddeath.core.user.IUser;
import rx.Observable;

import javax.annotation.Generated;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 17/03/2015.
 */
@Generated(
        value = "com.xxxx",
        date = "2014/05/01",
        comments = "Generated for servcice $"
)
public class TestServiceRestClientImpl implements ITestService {
    private IRestServiceClient serviceClient;

    public void setServiceClient(IRestServiceClient serviceClient){
        this.serviceClient = serviceClient;
    }

    @Override
    public Observable<Result> runWithRes(IGlobalContext ctxt, Input input) {
        RxJavaWebTarget target = serviceClient.getInstance();
        target = target.path(String.format("toto/%s/tuto/%s", input.rootId, input.id));

        return target.request(MediaType.APPLICATION_JSON_TYPE)
                       // .header(GlobalContextProvider.CONTEXT_HEADER, transcoder.encode(ctxt))
                        .property(IServiceClient.CONTEXT_PROPERTY,ctxt)
                        .post(
                        Entity.entity(input, MediaType.APPLICATION_JSON_TYPE),
                        new GenericType<>(Result.class)
                );
    }

    @Override
    public Observable<Result> getWithRes(String rootId, String id) {
        RxJavaWebTarget target = serviceClient
                .getInstance()
                .path(String.format("toto/%s/tuto/%s", rootId, id));

        return
                target.request(MediaType.APPLICATION_JSON_TYPE)
                        .get(
                                new GenericType<>(Result.class)
                        );
    }

    @Override
    public Observable<Result> putWithQuery(String rootId, String id) {
        RxJavaWebTarget target = serviceClient.getInstance();
        target = target.path(String.format("toto/%s", rootId));
        target = target.queryParam("id",id);
        return target.request(MediaType.APPLICATION_JSON_TYPE)

                        .put(
                                null,
                                new GenericType<>(Result.class)
                        );
    }

    @Override
    public Observable<TestingDocument> initDocument(IGlobalContext ctxt) {
        RxJavaWebTarget target = serviceClient.getInstance();
        target = target.property("IGlobalContext",ctxt);
        target = target.path(String.format("testingDocument"));
        return
                target.request(MediaType.APPLICATION_JSON_TYPE)
                        .post(
                                null,
                                new GenericType<>(TestingDocument.class)
                        );
    }

    @Override
    public Observable<TestingDocument> initDocument(IUser user) {
        RxJavaWebTarget target = serviceClient.getInstance();
        target = target.path(String.format("testingDocument"));
        return target.request(MediaType.APPLICATION_JSON_TYPE)
                        .property(IServiceClient.USER_PROPERTY,user)
                        .post(
                                null,
                                new GenericType<>(TestingDocument.class)
                        );
    }
}

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


import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextTranscoder;
import com.dreameddeath.core.service.utils.ServiceJacksonObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import rx.Observable;

import javax.annotation.Generated;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.Future;

/**
 * Created by CEAJ8230 on 17/03/2015.
 */
@Generated(
        value = "com.xxxx",
        date = "2014/05/01",
        comments = "Generated for servcice $"
)
public class TestServiceRestClientImpl implements ITestService {
    private IGlobalContextTranscoder _transcoder;
    private ServiceClientFactory _serviceClientFactory;

    public void setContextTranscoder(IGlobalContextTranscoder transcoder){
        _transcoder = transcoder;
    }

    public void setServiceClientFactory(ServiceClientFactory clientFactory){
        _serviceClientFactory = clientFactory;
    }

    @Override
    public Observable<Result> runWithRes(IGlobalContext ctxt, Input input) {
        Future<Result> responseFuture=
                _serviceClientFactory.getClient("testService", "1.0")
                .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                .path(String.format("toto/%s/tuto/%s", input.rootId, input.id))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("X-CONTEXT", _transcoder.encode(ctxt))
                .async()
                .post(
                        Entity.entity(input, MediaType.APPLICATION_JSON_TYPE),
                        new GenericType<>(Result.class)
                );

        return Observable.from(responseFuture);
    }
}

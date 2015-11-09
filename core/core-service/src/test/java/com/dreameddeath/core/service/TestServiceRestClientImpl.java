/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.service;


import com.dreameddeath.core.json.JsonProviderFactory;
import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextTranscoder;
import com.dreameddeath.core.service.swagger.TestingDocument;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import rx.Observable;

import javax.annotation.Generated;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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
    private IGlobalContextTranscoder transcoder;
    private ServiceClientFactory serviceClientFactory;

    public void setContextTranscoder(IGlobalContextTranscoder transcoder){
        this.transcoder = transcoder;
    }

    public void setServiceClientFactory(ServiceClientFactory clientFactory){
        serviceClientFactory = clientFactory;
    }

    @Override
    public Observable<Result> runWithRes(IGlobalContext ctxt, Input input) {
        WebTarget target = serviceClientFactory.getClient("testService", "1.0");
        target = target.register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR));
        target = target.path(String.format("toto/%s/tuto/%s", input.rootId, input.id));

        return Observable.from(
                target.request(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-CONTEXT", transcoder.encode(ctxt))
                        .async().post(
                        Entity.entity(input, MediaType.APPLICATION_JSON_TYPE),
                        new GenericType<>(Result.class)
                ));
    }

    @Override
    public Observable<Result> getWithRes(String rootId, String id) {
        WebTarget target = serviceClientFactory.getClient("testService", "1.0");
        target = target.register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR));
        target = target.path(String.format("toto/%s/tuto/%s", rootId, id));

        return Observable.from(
                target.request(MediaType.APPLICATION_JSON_TYPE)
                        .async()
                        .get(
                                new GenericType<>(Result.class)
                        ));
    }

    @Override
    public Observable<Result> putWithQuery(String rootId, String id) {
        WebTarget target = serviceClientFactory.getClient("testService", "1.0");
        target = target.register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR));
        target = target.path(String.format("toto/%s", rootId));
        target = target.queryParam("id",id);
        return Observable.from(
                target.request(MediaType.APPLICATION_JSON_TYPE)
                        .async()
                        .put(
                                null,
                                new GenericType<>(Result.class)
                        ));
    }

    @Override
    public Observable<TestingDocument> initDocument(IGlobalContext ctxt) {
        WebTarget target = serviceClientFactory.getClient("testService", "1.0");
        target = target.register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR));
        target = target.path(String.format("testingDocument"));
        return Observable.from(
                target.request(MediaType.APPLICATION_JSON_TYPE)
                        .async()
                        .post(
                                null,
                                new GenericType<>(TestingDocument.class)
                        ));
    }
}

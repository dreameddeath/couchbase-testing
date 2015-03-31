package com.dreameddeath.core.service;


import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextTranscoder;
import rx.Observable;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.Future;

/**
 * Created by CEAJ8230 on 17/03/2015.
 */
public class TestServiceRestClientImpl implements ITestService {
    IGlobalContextTranscoder _transcoder;
    ServiceClientFactory _serviceClientFactory;

    @Override
    public Observable<Result> runWithRes(IGlobalContext ctxt, Input input) {
        Future<Result> responseFuture=
                _serviceClientFactory.getClient("testService", "v1.0")
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

/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.dreameddeath.core.service.client.rest.rxjava;

import io.reactivex.Single;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

/**
 * Created by Christophe Jeunesse on 17/09/2016.
 */
public class RxJavaInvoker {
    public static final String HTTP_METHOD_TRACE = "TRACE";
    private final AsyncInvoker origInvoker;

    public RxJavaInvoker(AsyncInvoker origInvoker) {
        this.origInvoker = origInvoker;
    }

    public Single<Response> get() {
        return method(HttpMethod.GET);
    }

    public <T> Single<T> get(Class<T> responseType) {
        return method(HttpMethod.GET,responseType);
    }
    
    public <T> Single<T> get(GenericType<T> responseType) {
        return method(HttpMethod.GET,responseType);
    }

    public Single<Response> put(Entity<?> entity) {
        return method(HttpMethod.PUT,entity);
    }

    public <T> Single<T> put(Entity<?> entity, Class<T> responseType) {
        return method(HttpMethod.PUT,entity,responseType);
    }

    public <T> Single<T> put(Entity<?> entity, GenericType<T> responseType) {
        return method(HttpMethod.PUT,entity,responseType);
    }

    public Single<Response> post(Entity<?> entity) {
        return method(HttpMethod.POST,entity);
    }

    public <T> Single<T> post(Entity<?> entity, Class<T> responseType) {
        return method(HttpMethod.POST,entity,responseType);
    }

    public <T> Single<T> post(Entity<?> entity, GenericType<T> responseType) {
        return method(HttpMethod.POST,entity,responseType);
    }

    public Single<Response> delete() {
        return method(HttpMethod.DELETE);
    }

    public <T> Single<T> delete(Class<T> responseType) {
        return method(HttpMethod.DELETE,responseType);
    }

    public <T> Single<T> delete(GenericType<T> responseType) {
        return method(HttpMethod.DELETE,responseType);
    }

    public Single<Response> head() {
        return method(HttpMethod.HEAD);
    }

    public Single<Response> options() {
        return method(HttpMethod.OPTIONS);
    }

    public <T> Single<T> options(Class<T> responseType) {
        return method(HttpMethod.OPTIONS,responseType);
    }

    public <T> Single<T> options(GenericType<T> responseType) {
        return method(HttpMethod.OPTIONS,responseType);
    }

    public Single<Response> trace() {
        return method(HTTP_METHOD_TRACE);
    }

    public <T> Single<T> trace(Class<T> responseType) {
        return method(HTTP_METHOD_TRACE,responseType);
    }

    public <T> Single<T> trace(GenericType<T> responseType) {
        return method(HTTP_METHOD_TRACE,responseType);
    }


    public Single<Response> method(String name) {
        return method(name,null,(GenericType<Response>)null);
    }

    public <T> Single<T> method(String name, Class<T> responseType) {
        return method(name,null,responseType);
    }

    public <T> Single<T> method(String name, GenericType<T> responseType) {
        return method(name,null,responseType);
    }


    public Single<Response> method(String name, Entity<?> entity) {
        return method(name,entity,(GenericType<Response>)null);
    }

    public <T> Single<T> method(String name, Entity<?> entity, Class<T> responseType) {
        return method(name,entity,new GenericType<>(responseType));
    }


    public <T> Single<T> method(String name, Entity<?> entity, GenericType<T> responseType) {
        return buildObservableInvocation(name,entity,responseType);
    }

    private <T> Single<T> buildObservableInvocation(final String method, final Entity<?> entity, final GenericType<T> responseType){
        return Single.fromPublisher(emitter->{
                InvocationCallback<Response> callback = new InvocationCallback<Response>(){
                    @Override
                    public void completed(Response o) {
                        if(responseType==null){
                            emitter.onNext((T) o);
                            emitter.onComplete();
                        }
                        else {
                            try {
                                emitter.onNext(o.readEntity(responseType));
                                emitter.onComplete();
                            } catch (Throwable e) {
                                emitter.onError(e);
                            }
                        }

                    }

                    @Override
                    public void failed(Throwable throwable) {
                        emitter.onError(throwable);
                    }
                };
                if(entity==null){origInvoker.method(method,callback);}
                else{origInvoker.method(method,entity,callback);}
        });
    }



}

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

package com.dreameddeath.core.service.client.rest.rxjava;

import rx.Observable;
import rx.schedulers.Schedulers;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Future;

/**
 * Created by Christophe Jeunesse on 17/09/2016.
 */
public class RxJavaInvoker {
    private final AsyncInvoker origInvoker;

    public RxJavaInvoker(AsyncInvoker origInvoker) {
        this.origInvoker = origInvoker;
    }

    public Observable<Response> get() {
        return build(origInvoker.get());
    }

    public <T> Observable<T> get(Class<T> responseType) {
        return build(origInvoker.get(responseType));
    }
    
    public <T> Observable<T> get(GenericType<T> responseType) {
        return build(origInvoker.get(responseType));
    }

    public Observable<Response> put(Entity<?> entity) {
        return build(origInvoker.put(entity));
    }

    public <T> Observable<T> put(Entity<?> entity, Class<T> responseType) {
        return build(origInvoker.put(entity,responseType));
    }

    public <T> Observable<T> put(Entity<?> entity, GenericType<T> responseType) {
        return build(origInvoker.put(entity,responseType));
    }

    public Observable<Response> post(Entity<?> entity) {
        return build(origInvoker.post(entity));
    }

    public <T> Observable<T> post(Entity<?> entity, Class<T> responseType) {
        return build(origInvoker.post(entity,responseType));
    }

    public <T> Observable<T> post(Entity<?> entity, GenericType<T> responseType) {
        return build(origInvoker.post(entity,responseType));
    }

    public Observable<Response> delete() {
        return build(origInvoker.delete());
    }

    public <T> Observable<T> delete(Class<T> responseType) {
        return build(origInvoker.delete(responseType));
    }

    public <T> Observable<T> delete(GenericType<T> responseType) {
        return build(origInvoker.delete(responseType));
    }

    public Observable<Response> head() {
        return build(origInvoker.head());
    }

    public Observable<Response> options() {
        return build(origInvoker.options());
    }

    public <T> Observable<T> options(Class<T> responseType) {
        return build(origInvoker.options(responseType));
    }

    public <T> Observable<T> options(GenericType<T> responseType) {
        return build(origInvoker.options(responseType));
    }

    public Observable<Response> trace() {
        return build(origInvoker.trace());
    }

    public <T> Observable<T> trace(Class<T> responseType) {
        return build(origInvoker.trace(responseType));
    }

    public <T> Observable<T> trace(GenericType<T> responseType) {
        return build(origInvoker.trace(responseType));
    }


    public Observable<Response> method(String name) {
        return build(origInvoker.method(name));
    }

    public <T> Observable<T> method(String name, Class<T> responseType) {
        return build(origInvoker.method(name,responseType));
    }

    public <T> Observable<T> method(String name, GenericType<T> responseType) {
        return build(origInvoker.method(name,responseType));
    }


    public Observable<Response> method(String name, Entity<?> entity) {
        return build(origInvoker.method(name,entity));
    }

    public <T> Observable<T> method(String name, Entity<?> entity, Class<T> responseType) {
        return build(origInvoker.method(name,entity,responseType));
    }


    public <T> Observable<T> method(String name, Entity<?> entity, GenericType<T> responseType) {
        return build(origInvoker.method(name,entity,responseType));
    }


    private <T> Observable<T> build(Future<T> future){
        return Observable.from(future).observeOn(Schedulers.io());
    }
}

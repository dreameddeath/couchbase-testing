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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

/**
 * Created by Christophe Jeunesse on 18/09/2016.
 */
public class RxJavaSyncInvoker {
    private final RxJavaInvoker origInvoker;

    public RxJavaSyncInvoker(RxJavaInvoker origInvoker) {
        this.origInvoker = origInvoker;
    }

    public Response get() {
        return build(origInvoker.get());
    }

    public <T> T get(Class<T> responseType) {
        return build(origInvoker.get(responseType));
    }

    public <T> T get(GenericType<T> responseType) {
        return build(origInvoker.get(responseType));
    }

    public Response put(Entity<?> entity) {
        return build(origInvoker.put(entity));
    }

    public <T> T put(Entity<?> entity, Class<T> responseType) {
        return build(origInvoker.put(entity,responseType));
    }

    public <T> T put(Entity<?> entity, GenericType<T> responseType) {
        return build(origInvoker.put(entity,responseType));
    }

    public Response post(Entity<?> entity) {
        return build(origInvoker.post(entity));
    }

    public <T> T post(Entity<?> entity, Class<T> responseType) {
        return build(origInvoker.post(entity,responseType));
    }

    public <T> T post(Entity<?> entity, GenericType<T> responseType) {
        return build(origInvoker.post(entity,responseType));
    }

    public Response delete() {
        return build(origInvoker.delete());
    }

    public <T> T delete(Class<T> responseType) {
        return build(origInvoker.delete(responseType));
    }

    public <T> T delete(GenericType<T> responseType) {
        return build(origInvoker.delete(responseType));
    }

    public Response head() {
        return build(origInvoker.head());
    }

    public Response options() {
        return build(origInvoker.options());
    }

    public <T> T options(Class<T> responseType) {
        return build(origInvoker.options(responseType));
    }

    public <T> T options(GenericType<T> responseType) {
        return build(origInvoker.options(responseType));
    }

    public Response trace() {
        return build(origInvoker.trace());
    }

    public <T> T trace(Class<T> responseType) {
        return build(origInvoker.trace(responseType));
    }

    public <T> T trace(GenericType<T> responseType) {
        return build(origInvoker.trace(responseType));
    }


    public Response method(String name) {
        return build(origInvoker.method(name));
    }

    public <T> T method(String name, Class<T> responseType) {
        return build(origInvoker.method(name,responseType));
    }

    public <T> T method(String name, GenericType<T> responseType) {
        return build(origInvoker.method(name,responseType));
    }


    public Response method(String name, Entity<?> entity) {
        return build(origInvoker.method(name,entity));
    }

    public <T> T method(String name, Entity<?> entity, Class<T> responseType) {
        return build(origInvoker.method(name,entity,responseType));
    }


    public <T> T method(String name, Entity<?> entity, GenericType<T> responseType) {
        return build(origInvoker.method(name,entity,responseType));
    }


    private <T> T build(Observable<T> observable){
        return observable.toBlocking().single();
    }
}

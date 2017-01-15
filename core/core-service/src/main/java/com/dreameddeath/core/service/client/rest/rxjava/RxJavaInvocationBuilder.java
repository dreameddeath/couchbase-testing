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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.util.Locale;

/**
 * Created by Christophe Jeunesse on 17/09/2016.
 */
public class RxJavaInvocationBuilder {
    private final Invocation.Builder origBuilder;
    
    public RxJavaInvocationBuilder(Invocation.Builder request) {
        origBuilder = request;
    }
    
    public RxJavaInvocationBuilder accept(String... mediaTypes) {
        return new RxJavaInvocationBuilder(origBuilder.accept(mediaTypes));
    }
    
    public RxJavaInvocationBuilder accept(MediaType... mediaTypes) {
        return new RxJavaInvocationBuilder(origBuilder.accept(mediaTypes));
    }
    
    public RxJavaInvocationBuilder acceptLanguage(Locale... locales) {
        return new RxJavaInvocationBuilder(origBuilder.acceptLanguage(locales));
    }
    
    public RxJavaInvocationBuilder acceptLanguage(String... locales) {
        return new RxJavaInvocationBuilder(origBuilder.acceptLanguage(locales));
    }
    
    public RxJavaInvocationBuilder acceptEncoding(String... encodings) {
        return new RxJavaInvocationBuilder(origBuilder.acceptEncoding(encodings));
    }
    
    public RxJavaInvocationBuilder cookie(Cookie cookie) {
        return new RxJavaInvocationBuilder(origBuilder.cookie(cookie));
    }
    
    public RxJavaInvocationBuilder cookie(String name, String value) {
        return new RxJavaInvocationBuilder(origBuilder.cookie(name,value));
    }
    
    public RxJavaInvocationBuilder cacheControl(CacheControl cacheControl) {
        return new RxJavaInvocationBuilder(origBuilder.cacheControl(cacheControl));
    }
    
    public RxJavaInvocationBuilder header(String name, Object value) {
        return new RxJavaInvocationBuilder(origBuilder.header(name,value));
    }
    
    public RxJavaInvocationBuilder headers(MultivaluedMap<String, Object> headers) {
        return new RxJavaInvocationBuilder(origBuilder.headers(headers));
    }
    
    public RxJavaInvocationBuilder property(String name, Object value) {
        return new RxJavaInvocationBuilder(origBuilder.property(name,value));
    }

    public Single<Response> get() {
        return toAsync().get();
    }

    public <T> Single<T> get(Class<T> responseType) {
        return toAsync().get(responseType);
    }

    public <T> Single<T> get(GenericType<T> responseType) {
        return toAsync().get(responseType);
    }

    public Single<Response> put(Entity<?> entity) {
        return toAsync().put(entity);
    }

    public <T> Single<T> put(Entity<?> entity, Class<T> responseType) {
        return toAsync().put(entity,responseType);
    }

    public <T> Single<T> put(Entity<?> entity, GenericType<T> responseType) {
        return toAsync().put(entity,responseType);
    }

    public Single<Response> post(Entity<?> entity) {
        return toAsync().post(entity);
    }

    public <T> Single<T> post(Entity<?> entity, Class<T> responseType) {
        return toAsync().post(entity,responseType);
    }

    public <T> Single<T> post(Entity<?> entity, GenericType<T> responseType) {
        return toAsync().post(entity,responseType);
    }

    public Single<Response> delete() {
        return toAsync().delete();
    }

    public <T> Single<T> delete(Class<T> responseType) {
        return toAsync().delete(responseType);
    }

    public <T> Single<T> delete(GenericType<T> responseType) {
        return toAsync().delete(responseType);
    }

    public Single<Response> head() {
        return toAsync().head();
    }

    public Single<Response> options() {
        return toAsync().options();
    }

    public <T> Single<T> options(Class<T> responseType) {
        return toAsync().options(responseType);
    }

    public <T> Single<T> options(GenericType<T> responseType) {
        return toAsync().options(responseType);
    }

    public Single<Response> trace() {
        return toAsync().trace();
    }

    public <T> Single<T> trace(Class<T> responseType) {
        return toAsync().trace(responseType);
    }

    public <T> Single<T> trace(GenericType<T> responseType) {
        return toAsync().trace(responseType);
    }


    public Single<Response> method(String name) {
        return toAsync().method(name);
    }

    public <T> Single<T> method(String name, Class<T> responseType) {
        return toAsync().method(name,responseType);
    }

    public <T> Single<T> method(String name, GenericType<T> responseType) {
        return toAsync().method(name,responseType);
    }


    public Single<Response> method(String name, Entity<?> entity) {
        return toAsync().method(name,entity);
    }

    public <T> Single<T> method(String name, Entity<?> entity, Class<T> responseType) {
        return toAsync().method(name,entity,responseType);
    }


    public <T> Single<T> method(String name, Entity<?> entity, GenericType<T> responseType) {
        return toAsync().method(name,entity,responseType);
    }
    
    private <T> RxJavaInvoker toAsync(){
        return new RxJavaInvoker(origBuilder.async());
    } 

    public RxJavaSyncInvoker sync(){
        return new RxJavaSyncInvoker(toAsync());
    }
}

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

package com.dreameddeath.core.couchbase.rxjava;


import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;

/**
 * Created by ceaj8230 on 07/01/2017.
 */
public class RxJavaWrapper {
    public static <T> io.reactivex.Single<T> toRxJava2Single(rx.Observable<T> source){
        return io.reactivex.Single.fromPublisher(publisher-> source.subscribe(
                publisher::onNext,
                publisher::onError,
                publisher::onComplete
        ));
    }

    public static <T> io.reactivex.Single<T> toRxJava2Single(rx.Observable<T> source, String key){
        return io.reactivex.Single.fromPublisher(publisher-> source
                    .switchIfEmpty(rx.Observable.error(new DocumentNotFoundException(key, "Cannot find document using key <" + key + ">")))
                    .subscribe(
                            publisher::onNext,
                            publisher::onError,
                            publisher::onComplete
                    ));
    }

    public static <T> io.reactivex.Observable<T> toRxJava2Observable(rx.Observable<T> source) {
        return io.reactivex.Observable.fromPublisher(publisher-> source.subscribe(
                publisher::onNext,
                publisher::onError,
                publisher::onComplete
        ));
    }
}

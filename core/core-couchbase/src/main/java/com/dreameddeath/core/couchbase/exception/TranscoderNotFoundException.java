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

package com.dreameddeath.core.couchbase.exception;

/**
 * Created by Christophe Jeunesse on 21/10/2015.
 */
public class TranscoderNotFoundException extends Exception {
    public TranscoderNotFoundException(Class clazz,Throwable e){
        super("Cannot find/init transcoder for class "+clazz.getName(),e);
    }
}

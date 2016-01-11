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

package com.dreameddeath.core.json;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * Created by Christophe Jeunesse on 11/01/2016.
 */
public interface IProviderInterceptor<T> {
    boolean isApplicableTo(Object obj);
    void preWriteTo(T value, Annotation[] annotations, MultivaluedMap<String, Object> httpHeaders) throws IOException;
    void postReadFrom(T value, Annotation[] annotations, MultivaluedMap<String, String> httpHeaders);
}

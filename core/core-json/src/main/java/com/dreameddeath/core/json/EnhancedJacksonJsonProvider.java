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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 11/01/2016.
 */
public class EnhancedJacksonJsonProvider extends JacksonJsonProvider {
    private final List<IProviderInterceptor> interceptors;

    public EnhancedJacksonJsonProvider(ObjectMapper mapper, Collection<IProviderInterceptor> interceptors) {
        super(mapper);
        this.interceptors = new ArrayList<>(interceptors);
    }

    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        for(IProviderInterceptor interceptor : interceptors){
            if(interceptor.isApplicableTo(value)){
                interceptor.preWriteTo(value,annotations,httpHeaders);
            }
        }
        super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        Object result  = super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
        for(IProviderInterceptor interceptor : interceptors){
            if(interceptor.isApplicableTo(result)){
                interceptor.postReadFrom(result,annotations,httpHeaders);
            }
        }
        return result;
    }
}

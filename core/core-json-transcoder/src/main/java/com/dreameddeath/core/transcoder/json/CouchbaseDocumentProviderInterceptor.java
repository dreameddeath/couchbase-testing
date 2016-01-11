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

package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.json.IProviderInterceptor;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * Created by Christophe Jeunesse on 11/01/2016.
 */
public class CouchbaseDocumentProviderInterceptor implements IProviderInterceptor<CouchbaseDocument> {
    public static final String HTTP_HEADER_DOC_KEY = "X-DOCUMENT-KEY";
    public static final String HTTP_HEADER_DOC_REV = "X-DOCUMENT-REV";
    public static final String HTTP_HEADER_DOC_FLAGS = "X-DOCUMENT-FLAGS";


    @Override
    public boolean isApplicableTo(Object obj) {
        return obj instanceof CouchbaseDocument;
    }

    @Override
    public void preWriteTo(CouchbaseDocument value, Annotation[] annotations, MultivaluedMap<String, Object> httpHeaders) throws IOException {
        CouchbaseDocument.BaseMetaInfo info = value.getBaseMeta();
        if(info.getKey()!=null) {
            httpHeaders.add(HTTP_HEADER_DOC_KEY, info.getKey());
        }
        if(info.getCas()!=0) {
            httpHeaders.add(HTTP_HEADER_DOC_REV, Long.toString(info.getCas()));
        }
        if(info.getEncodedFlags()!=0) {
            httpHeaders.add(HTTP_HEADER_DOC_FLAGS,info.getEncodedFlags().toString());
        }
    }

    @Override
    public void postReadFrom(CouchbaseDocument value, Annotation[] annotations, MultivaluedMap<String, String> httpHeaders) {
        CouchbaseDocument.BaseMetaInfo info = value.getBaseMeta();
        String flagsStr = httpHeaders.getFirst(HTTP_HEADER_DOC_FLAGS);
        if(flagsStr!=null) {
            info.setEncodedFlags(Integer.parseInt(flagsStr));
        }
        String casStr = httpHeaders.getFirst(HTTP_HEADER_DOC_REV);
        if(casStr!=null) {
            try {
                info.setCas(Long.parseLong(casStr));
            }
            catch(NumberFormatException e){
                //ignore error
            }
        }
        info.setKey(httpHeaders.getFirst(HTTP_HEADER_DOC_KEY));
    }
}

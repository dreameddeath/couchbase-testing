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

package com.dreameddeath.core.helper.service;

import com.dreameddeath.core.model.document.CouchbaseDocument;

import javax.ws.rs.core.Response;

/**
 * Created by Christophe Jeunesse on 15/04/2015.
 */
public class DaoHelperServiceUtils {
    public static final String HTTP_HEADER_DOC_KEY = "X-DOCUMENT-KEY";
    public static final String HTTP_HEADER_DOC_REV = "X-DOCUMENT-REV";
    public static final String HTTP_HEADER_DOC_FLAGS = "X-DOCUMENT-FLAGS";
    public static final String HTTP_HEADER_QUERY_TOTAL_ROWS = "X-DOCUMENT-QUERY-TOTAL-ROWS";


    public static <T extends CouchbaseDocument> T readFromResponse(Response response, Class<T> clazz){
        T result = response.readEntity(clazz);
        CouchbaseDocument.BaseMetaInfo info = result.getBaseMeta();
        info.setEncodedFlags(Integer.parseInt(response.getHeaderString(HTTP_HEADER_DOC_FLAGS)));
        info.setCas(Long.parseLong(response.getHeaderString(HTTP_HEADER_DOC_REV)));
        info.setKey(response.getHeaderString(HTTP_HEADER_DOC_KEY));
        info.setStateSync();
        return result;
    }
}

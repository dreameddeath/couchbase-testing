/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.service;

import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.google.common.base.Preconditions;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Christophe Jeunesse on 17/09/2016.
 */
public class TestTraceIdFeature implements Feature,ClientResponseFilter {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(this);
        return true;
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        String httpTraceId= responseContext.getHeaderString(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID);
        Preconditions.checkNotNull(httpTraceId);
        if(requestContext.getUri().getPath().contains("traceId")){
            InputStream entityStream = responseContext.getEntityStream();
            String resultTraceId = IOUtils.toString(entityStream,"UTF-8");
            ByteArrayInputStream baos = new ByteArrayInputStream(resultTraceId.getBytes());
            responseContext.setEntityStream(baos);
            Preconditions.checkArgument(httpTraceId.equals(resultTraceId));
        }
    }
}

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

package com.dreameddeath.couchbase.core.process.remote.json;

import com.dreameddeath.core.json.IProviderInterceptor;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.couchbase.core.process.remote.model.rest.StateInfo;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 26/02/2016.
 */
public class JobResultProviderInterceptor implements IProviderInterceptor<RemoteJobResultWrapper> {
    public static final String HTTP_HEADER_JOB_STATE = "X-REMOTE-JOB-STATE";
    public static final String HTTP_HEADER_JOB_ID = "X-REMOTE-JOB-ID";
    public static final String HTTP_HEADER_JOB_LASTERROR = "X-REMOTE-JOB-LAST-ERROR";

    @Override
    public boolean isApplicableTo(Object obj) {
        return obj instanceof RemoteJobResultWrapper;
    }

    @Override
    public void preWriteTo(RemoteJobResultWrapper value, Annotation[] annotations, MultivaluedMap<String, Object> httpHeaders) throws IOException {
        if(value.getJobId()!=null){
            httpHeaders.add(HTTP_HEADER_JOB_ID,value.getJobId().toString());
        }
        if(value.getJobStateInfo()!=null){
            httpHeaders.add(HTTP_HEADER_JOB_LASTERROR,value.getJobStateInfo().lastRunError);
            httpHeaders.add(HTTP_HEADER_JOB_STATE,value.getJobStateInfo().state.toString());
        }

    }

    @Override
    public void postReadFrom(RemoteJobResultWrapper value, Annotation[] annotations, MultivaluedMap<String, String> httpHeaders) {
        if(httpHeaders.containsKey(HTTP_HEADER_JOB_ID)){
            value.setJodId(UUID.fromString(httpHeaders.getFirst(HTTP_HEADER_JOB_ID)));
        }
        StateInfo info=new StateInfo();
        value.setJobStateInfo(info);
        if(httpHeaders.containsKey(HTTP_HEADER_JOB_LASTERROR)){
            info.lastRunError = httpHeaders.getFirst(HTTP_HEADER_JOB_LASTERROR);
        }
        if(httpHeaders.containsKey(HTTP_HEADER_JOB_STATE)){
            info.state = StateInfo.State.valueOf(httpHeaders.getFirst(HTTP_HEADER_JOB_STATE));
        }
    }
}

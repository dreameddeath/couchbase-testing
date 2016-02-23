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

package com.dreameddeath.couchbase.core.process.rest.process.rest;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobUpdate;
import com.dreameddeath.couchbase.core.process.remote.model.rest.TestDocJobUpdateRequest;
import com.dreameddeath.couchbase.core.process.remote.model.rest.TestDocJobUpdateResult;
import com.dreameddeath.couchbase.core.process.remote.service.AbstractRemoteJobRestService;

import javax.ws.rs.Path;

/**
 * Created by Christophe Jeunesse on 15/01/2016.
 */
@ServiceDef(domain = "tests",name="testdocjobupdate",version = "1.0.0")
@Path("testdocjobs/update")
public class RestTestDocJobUpdateService extends AbstractRemoteJobRestService<TestDocJobUpdate,TestDocJobUpdateRequest,TestDocJobUpdateResult> {
    @Override
    protected TestDocJobUpdate buildJobFromRequest(TestDocJobUpdateRequest request) {
        return request.buildJob();
    }

    @Override
    protected TestDocJobUpdateResult buildResponse(TestDocJobUpdate response) {
        return new TestDocJobUpdateResult(response);
    }

    @Override
    protected Class<TestDocJobUpdate> getJobClass() {
        return TestDocJobUpdate.class;
    }
}

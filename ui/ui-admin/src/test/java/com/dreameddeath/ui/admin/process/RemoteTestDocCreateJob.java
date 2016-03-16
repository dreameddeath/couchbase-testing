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

package com.dreameddeath.ui.admin.process;

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.process.model.base.AbstractJob;
import com.dreameddeath.couchbase.core.process.remote.model.RemoteJobProcessTask;
import com.dreameddeath.ui.admin.process.published.TestDocCreateJobRequest;
import com.dreameddeath.ui.admin.process.published.TestDocCreateJobResponse;

/**
 * Created by Christophe Jeunesse on 13/03/2016.
 */
@DocumentDef(domain = "test")
public class RemoteTestDocCreateJob extends AbstractJob {
    @DocumentProperty
    public String remoteName;

    @DocumentDef(domain = "test",version="1.0.0")
    public static class RemoteTestDocCreateTask extends RemoteJobProcessTask<TestDocCreateJobRequest,TestDocCreateJobResponse> {
        @DocumentProperty("key")
        public String key;
    }
}

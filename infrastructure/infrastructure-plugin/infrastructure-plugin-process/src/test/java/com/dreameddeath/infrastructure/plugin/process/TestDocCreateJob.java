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

package com.dreameddeath.infrastructure.plugin.process;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateTask;
import com.dreameddeath.couchbase.core.process.remote.annotation.Request;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;
import com.dreameddeath.couchbase.core.process.remote.annotation.Result;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
@DocumentEntity(domain = "test") @RestExpose(rootPath = "testdoc/create",domain = "test",name = "testdoccreatejob")
public class TestDocCreateJob extends AbstractJob {
    @DocumentProperty("name")  @Request
    public String name;
    @DocumentProperty("key") @Result
    public String key;

    @DocumentEntity(domain = "test",version="1.0.0")
    public static class TestDocCreateTask extends DocumentCreateTask<TestDocProcess> { }
}

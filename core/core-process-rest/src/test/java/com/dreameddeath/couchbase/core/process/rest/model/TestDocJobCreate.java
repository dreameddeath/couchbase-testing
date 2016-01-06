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

package com.dreameddeath.couchbase.core.process.rest.model;

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.core.validation.annotation.Unique;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
@DocumentDef(domain="test")
public class TestDocJobCreate extends AbstractJob {
    @DocumentProperty @NotNull @Unique(nameSpace = "TestJobCreate")
    public String tempUid;
    @DocumentProperty @NotNull
    public String name;
    @DocumentProperty
    public Integer initIntValue;

    @DocumentProperty
    public String createdKey;
}

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

package com.dreameddeath.core.process.model;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractProcessCouchbaseDocument;
import com.dreameddeath.core.validation.annotation.Unique;

/**
 * Created by Christophe Jeunesse on 01/11/2016.
 */
@DocumentEntity(domain = "test",version = "1.0")
public class TestDoc extends AbstractProcessCouchbaseDocument {
    @DocumentProperty @Unique(nameSpace = "testDoc")
    public String name;
    @DocumentProperty
    public Integer intValue;
}

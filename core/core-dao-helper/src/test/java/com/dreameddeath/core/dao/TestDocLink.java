/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao;

import com.dreameddeath.core.model.business.BusinessCouchbaseDocumentLink;

/**
 * Created by ceaj8230 on 20/04/2015.
 */
public class TestDocLink extends BusinessCouchbaseDocumentLink<TestDoc> {
    public TestDocLink(){}
    public TestDocLink (TestDoc src){super(src);}
    public TestDocLink(TestDocLink srcLink){super(srcLink);}
}
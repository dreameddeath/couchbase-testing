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

package com.dreameddeath.core.service;

import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.swagger.TestingDocument;
import com.dreameddeath.core.user.IUser;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 05/03/2015.
 */
public interface ITestService {

    Observable<Result> runWithRes(IGlobalContext ctxt, Input input);
    Observable<Result> getWithRes(String rootId, String id);
    Observable<Result> putWithQuery(String rootId, String id);

    Observable<TestingDocument> initDocument(IGlobalContext ctxt);
    Observable<TestingDocument> initDocument(IUser user);

    class Result{
        public String result;
        public String id;
        public String rootId;
        public DateTime plusOneMonth;
    }

    class Input{
        public String id;
        public String rootId;
        public DateTime otherField;
    }
}

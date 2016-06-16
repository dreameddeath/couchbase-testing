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

package com.dreameddeath.core.validation;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.property.HasParent;

/**
 * Created by Christophe Jeunesse on 20/11/2014.
 */
public class ValidatorContext {
    private final ICouchbaseSession session;
    private final HasParent currParentElement;
    private final ValidatorContext parentContext;

    public ValidatorContext(ValidatorContext parentContext,HasParent currParentElement) {
        this.session = parentContext.session;
        this.currParentElement = currParentElement;
        this.parentContext = parentContext;
    }

    public ValidatorContext(ICouchbaseSession session){
        this.session=session;
        this.currParentElement=null;
        this.parentContext=null;
    }

    public ICouchbaseSession getSession(){return session;}

    public static ValidatorContext buildContext(ICouchbaseSession session){
        return new ValidatorContext(session);
    }

    public HasParent head() {
        return currParentElement;
    }
}

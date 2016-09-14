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

package com.dreameddeath.core.service.context.feature;

import com.dreameddeath.core.context.IContextFactory;
import com.dreameddeath.core.service.context.provider.ContextClientFilter;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Created by Christophe Jeunesse on 12/01/2016.
 */
public class ContextClientFeature implements Feature {
    private final ContextClientFilter contextClientFilter;

    public ContextClientFeature(IContextFactory contextFactory){
        contextClientFilter = new ContextClientFilter();
        contextClientFilter.setGlobalContextFactory(contextFactory);
    }

    @Override
    public boolean configure(FeatureContext featureContext) {
        featureContext.register(contextClientFilter);
        return true;
    }
}

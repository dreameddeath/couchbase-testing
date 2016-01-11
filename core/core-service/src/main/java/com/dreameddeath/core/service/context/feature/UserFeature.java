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

package com.dreameddeath.core.service.context.feature;

import com.dreameddeath.core.user.IUserFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.AbstractFeature;

/**
 * Created by Christophe Jeunesse on 11/01/2016.
 */
public class UserFeature extends AbstractFeature {
    private final IUserFactory factory;

    public UserFeature(IUserFactory userFactory){
        this.factory = userFactory;
    }

    public void initialize(Client client, Bus bus) {
        UserClientInInterceptor clientInInterceptor = new UserClientInInterceptor(factory);
        client.getInInterceptors().add(clientInInterceptor);
    }

}

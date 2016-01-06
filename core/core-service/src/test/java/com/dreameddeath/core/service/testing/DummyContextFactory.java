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

package com.dreameddeath.core.service.testing;

import com.dreameddeath.core.service.context.*;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.IUser;

/**
 * Created by Christophe Jeunesse on 06/01/2016.
 */
public class DummyContextFactory implements IGlobalContextFactory {
    @Override
    public String encode(IGlobalContext ctxt) {
return "";
}

    @Override
    public IGlobalContext decode(String encodedContext) {
        return null;
}

    @Override
    public IGlobalContext buildDefaultContext() {
        return buildContext(AnonymousUser.INSTANCE);
}

    @Override
    public IGlobalContext buildContext(IUser user) {
        return new IGlobalContext() {
            @Override
            public ICallerContext callerCtxt() {
                return null;
            }

            @Override
            public IExternalCallerContext externalCtxt() {
                return null;
            }

            @Override
            public IUserContext userCtxt() {
                return () -> user;
            }
        };
    }
}

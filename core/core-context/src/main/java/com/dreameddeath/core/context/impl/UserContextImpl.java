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

package com.dreameddeath.core.context.impl;

import com.dreameddeath.core.context.IUserContext;
import com.dreameddeath.core.user.IUser;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 12/09/2016.
 */
public class UserContextImpl implements IUserContext {
    private final IUser user;
    private final String userToken;

    @JsonCreator
    public UserContextImpl(@JsonProperty("token") String userToken) {
        this(IUserContext.builder().withToken(userToken));
    }


    public UserContextImpl(Builder builder){
        user=builder.getUser();
        userToken=builder.getToken();
    }

    @Override @JsonIgnore
    public IUser getUser() {
        return user;
    }

    @Override @JsonProperty("token")
    public String getToken() {
        return userToken;
    }
}

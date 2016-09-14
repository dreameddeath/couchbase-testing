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

package com.dreameddeath.core.user;

import com.dreameddeath.core.java.utils.StringUtils;
import com.google.common.base.Preconditions;

/**
 * Created by Christophe Jeunesse on 23/10/2015.
 */
public class StandardMockUserFactory implements IUserFactory {
    private final static String TOKEN_PREFIX = "MOCK_TOKEN:";
    @Override
    public IUser fromId(final String userId){
        if(AuthorizeAllUser.INSTANCE.getUserId().equals(userId)){
            return AuthorizeAllUser.INSTANCE;
        }
        else if(AnonymousUser.INSTANCE.getUserId().equals(userId)){
            return AnonymousUser.INSTANCE;
        }
        return new IUser() {
            @Override
            public String getUserId() {
                return userId;
            }

            @Override
            public Boolean hasRight(String name) {
                return false;
            }

            @Override
            public String getProperty(String name) {
                return null;
            }
        };
    }

    @Override
    public IUser fromToken(String token) {
        if(StringUtils.isEmpty(token)){
            return AnonymousUser.INSTANCE;
        }
        else {
            Preconditions.checkArgument(token.startsWith(TOKEN_PREFIX),"The token %s doesn't start with prefix %s",token,TOKEN_PREFIX);
            return fromId(token.substring(TOKEN_PREFIX.length()));
        }
    }

    @Override
    public String toToken(IUser user) {
        return TOKEN_PREFIX+user.getUserId();
    }

    @Override
    public IUser defaultUser() {
        return AnonymousUser.INSTANCE;
    }
}

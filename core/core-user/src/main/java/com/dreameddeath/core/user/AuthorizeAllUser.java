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

package com.dreameddeath.core.user;

/**
 * Created by Christophe Jeunesse on 23/10/2015.
 */
public class AuthorizeAllUser implements IUser {
    public static final AuthorizeAllUser INSTANCE = new AuthorizeAllUser();

    private AuthorizeAllUser(){}
    @Override
    public String getUserId() {
        return "<AllGranted>";
    }

    @Override
    public Boolean hasRight(String name) {
        return true;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }
}

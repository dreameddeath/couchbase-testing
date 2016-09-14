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

/**
 * Created by Christophe Jeunesse on 13/09/2016.
 */
public abstract class MinimalUser implements IUser {
    private final String userId;
    private final boolean authorizeResult;

    public MinimalUser(String userId, boolean authorizeResult) {
        this.userId = userId;
        this.authorizeResult = authorizeResult;
    }

    @Override
    public final String getUserId() {
        return userId;
    }

    @Override
    final public Boolean hasRight(String name) {
        return authorizeResult;
    }

    @Override
    public String getProperty(String name) {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(!(obj instanceof IUser)) return false;
        IUser target = (IUser)obj;
        return userId==target.getUserId() || (userId!=null && userId.equals(target.getUserId()));
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}

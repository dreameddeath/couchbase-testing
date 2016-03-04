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

package com.dreameddeath.couchbase.core.process.remote.model.rest;

/**
 * Created by Christophe Jeunesse on 05/01/2016.
 */
public enum ActionRequest {
    RESUME("resume"),
    CANCEL("cancel");

    private final String value;
    ActionRequest(String value){
        this.value = value;
    }

    public static ActionRequest fromValue(String value){
        for(ActionRequest action:ActionRequest.values()){
            if(action.value.equalsIgnoreCase(value)){
                return action;
            }
        }
        return null;
    }
}
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

package com.dreameddeath.core.notification.remote.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 13/10/2016.
 */
public class RemoteProcessingResult {
    @JsonProperty("notificationKey")
    private final String notificationKey;
    @JsonProperty("success")
    private final boolean isSuccess;
    @JsonProperty("errorMessage")
    private final String errorMessage;
    @JsonProperty("exceptionclass")
    private final String exceptionClass;

    @JsonCreator
    public RemoteProcessingResult(
            @JsonProperty("notificationKey") String notificationKey,
            @JsonProperty("success") boolean isSuccess,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("exceptionclass") String exceptionClass
    ) {
        this.notificationKey = notificationKey;
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
        this.exceptionClass = exceptionClass;
    }

    @JsonGetter("notificationKey")
    public String getNotificationKey() {
        return notificationKey;
    }

    @JsonGetter("success")
    public boolean isSuccess() {
        return isSuccess;
    }

    @JsonGetter("errorMessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonGetter("exceptionClass")
    public String getExceptionClass() {
        return exceptionClass;
    }
}

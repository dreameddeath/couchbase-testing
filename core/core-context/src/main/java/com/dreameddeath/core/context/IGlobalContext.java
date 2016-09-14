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

package com.dreameddeath.core.context;

/**
 * Created by Christophe Jeunesse on 05/03/2015.
 */
public interface IGlobalContext {
    String currentTraceId();
    ICallerContext callerCtxt();
    IExternalCallerContext externalCtxt();
    IUserContext userCtxt();
    String globalTraceId();

    public static Builder builder(){
        return new Builder();
    }
    class Builder {
        private String traceId=null;
        private String globalTraceId=null;
        private ICallerContext.Builder callerContextBuilder=null;
        private IUserContext.Builder userContextBuilder=null;
        private IExternalCallerContext.Builder externalContextBuilder=null;

        public String getTraceId() {
            return traceId;
        }

        public ICallerContext.Builder getCallerContextBuilder() {
            return callerContextBuilder;
        }

        public IUserContext.Builder getUserContextBuilder() {
            return userContextBuilder;
        }

        public IExternalCallerContext.Builder getExternalContextBuilder() {
            return externalContextBuilder;
        }

        public Builder withTraceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder withCallerContextBuilder(ICallerContext.Builder callerContextBuilder) {
            this.callerContextBuilder = callerContextBuilder;
            return this;
        }

        public Builder withUserContextBuilder(IUserContext.Builder userContextBuilder) {
            this.userContextBuilder = userContextBuilder;
            return this;
        }

        public Builder withExternalContextBuilder(IExternalCallerContext.Builder externalContextBuilder) {
            this.externalContextBuilder = externalContextBuilder;
            return this;
        }

        public Builder withExternalContext(IExternalCallerContext context){
            return withExternalContextBuilder(IExternalCallerContext.builder().from(context));
        }

        public Builder withCallerContext(ICallerContext context){
            return withCallerContextBuilder(ICallerContext.builder().from(context));
        }

        public Builder withUserContext(IUserContext context){
            return withUserContextBuilder(IUserContext.builder().from(context));
        }

        public Builder withGlobalTraceId(String globalTraceId) {
            this.globalTraceId=globalTraceId;
            return this;
        }

        public String getGlobalTraceId() {
            return globalTraceId;
        }
    }
}

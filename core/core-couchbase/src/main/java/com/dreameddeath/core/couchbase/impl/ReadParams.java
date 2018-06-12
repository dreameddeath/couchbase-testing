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

package com.dreameddeath.core.couchbase.impl;

import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 12/12/2014.
 */
public class ReadParams {
    private final String keyPrefix;
    private final ReadReplicateMode readMode;
    private final long timeOut;
    private final TimeUnit timeOutUnit;

    public ReadParams(Builder builder) {
        this.keyPrefix = builder.keyPrefix;
        this.readMode = builder.readMode;
        this.timeOut = builder.timeOut;
        this.timeOutUnit = builder.timeOutUnit;
    }

    public ReadReplicateMode getReadMode() {
        return readMode;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public TimeUnit getTimeOutUnit() {
        return timeOutUnit;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public static ReadParams create(){
        return new Builder().create();
    }

    public static Builder builder(){
        return new Builder();
    }

    public ReadParams with(ReadReplicateMode mode){
        return new Builder(this).with(mode).create();
    }

    public ReadParams with(long timeout,TimeUnit unit){
        return new Builder(this).with(timeout,unit).create();
    }
    public ReadParams with(String keyPrefix){
        return new Builder(this).with(keyPrefix).create();
    }

    public enum ReadReplicateMode {
        FROM_MASTER,
        FROM_REPLICATE,
        FROM_MASTER_THEN_REPLICATE
    }

    public static class Builder{
        private String keyPrefix=null;
        private ReadReplicateMode readMode=ReadReplicateMode.FROM_MASTER;
        private long timeOut=0;
        private TimeUnit timeOutUnit=null;

        public Builder(){

        }

        public Builder(ReadParams params){
            this.keyPrefix = params.keyPrefix;
            this.readMode = params.readMode;
            this.timeOut = params.timeOut;
            this.timeOutUnit = params.timeOutUnit;
        }

        public Builder with(ReadReplicateMode mode){
            this.readMode = mode;
            return this;
        }

        public Builder with(long timeout,TimeUnit unit){
            this.timeOut = timeout;
            this.timeOutUnit = unit;
            return this;
        }

        public Builder with(String keyPrefix){
            this.keyPrefix = keyPrefix;
            return this;
        }

        public ReadParams create(){
            return new ReadParams(this);
        }
    }
}

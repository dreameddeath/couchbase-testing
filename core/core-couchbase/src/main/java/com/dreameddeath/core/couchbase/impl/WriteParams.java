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
public class WriteParams {
    private String keyPrefix=null;
    private ReplicateToMode writeReplicateMode = ReplicateToMode.NONE;
    private PersistToMode writePersistMode = PersistToMode.NONE;


    private long timeOut=0;
    private TimeUnit timeOutUnit=null;

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
    public WriteParams with(String keyPrefix){this.keyPrefix = keyPrefix;return this;}

    public long getTimeOut() {
        return timeOut;
    }
    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
    public WriteParams with(long timeOut){this.timeOut = timeOut;return this;}

    public TimeUnit getTimeOutUnit() {
        return timeOutUnit;
    }
    public void setTimeOutUnit(TimeUnit timeOutUnit) {
        this.timeOutUnit = timeOutUnit;
    }
    public WriteParams with(TimeUnit unit){timeOutUnit = unit;return this;}

    public ReplicateToMode getWriteReplicateMode() {return writeReplicateMode;}
    public void setWriteReplicateMode(ReplicateToMode writeReplicateMode) {this.writeReplicateMode = writeReplicateMode;}
    public WriteParams with(ReplicateToMode mode){writeReplicateMode = mode;return this;}

    public PersistToMode getWritePersistMode() {return writePersistMode;}
    public void setWritePersistMode(PersistToMode writePersistMode) {this.writePersistMode = writePersistMode;}
    public WriteParams with(PersistToMode mode){writePersistMode = mode;return this;}

    public static WriteParams create(){return new WriteParams();}

    public enum ReplicateToMode {
        NONE, //
        ALL_SLAVES //
    }


    public enum PersistToMode{
        NONE, //PersistTo.NONE
        MASTER, //PersistTo.NONE
        MASTER_AND_ALL_SLAVES //PersistToMax
    }

}

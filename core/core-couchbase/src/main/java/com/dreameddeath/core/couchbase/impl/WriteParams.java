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
    private ReplicateToMode _writeReplicateMode = ReplicateToMode.NONE;
    private PersistToMode _writePersistMode = PersistToMode.NONE;


    private long _timeOut=0;
    private TimeUnit _timeOutUnit=null;


    public long getTimeOut() {
        return _timeOut;
    }
    public void setTimeOut(long timeOut) {
        _timeOut = timeOut;
    }
    public WriteParams with(long timeOut){_timeOut = timeOut;return this;}

    public TimeUnit getTimeOutUnit() {
        return _timeOutUnit;
    }
    public void setTimeOutUnit(TimeUnit timeOutUnit) {
        _timeOutUnit = timeOutUnit;
    }
    public WriteParams with(TimeUnit unit){_timeOutUnit = unit;return this;}

    public ReplicateToMode getWriteReplicateMode() {return _writeReplicateMode;}
    public void setWriteReplicateMode(ReplicateToMode writeReplicateMode) {_writeReplicateMode = writeReplicateMode;}
    public WriteParams with(ReplicateToMode mode){_writeReplicateMode = mode;return this;}

    public PersistToMode getWritePersistMode() {return _writePersistMode;}
    public void setWritePersistMode(PersistToMode writePersistMode) {_writePersistMode = writePersistMode;}
    public WriteParams with(PersistToMode mode){_writePersistMode = mode;return this;}

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

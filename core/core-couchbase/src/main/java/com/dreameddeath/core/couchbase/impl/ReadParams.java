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
    private ReadReplicateMode readMode=ReadReplicateMode.FROM_MASTER;
    private long timeOut=0;
    private TimeUnit timeOutUnit=null;

    public ReadReplicateMode getReadMode() {
        return readMode;
    }
    public void setReadMode(ReadReplicateMode readMode) {
        this.readMode = readMode;
    }

    public long getTimeOut() {
        return timeOut;
    }
    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public TimeUnit getTimeOutUnit() {
        return timeOutUnit;
    }
    public void setTimeOutUnit(TimeUnit timeOutUnit) {
        this.timeOutUnit = timeOutUnit;
    }

    public static ReadParams create(){return new ReadParams();}

    public ReadParams with(ReadReplicateMode mode){readMode = mode;return this;}
    public ReadParams with(long timeout,TimeUnit unit){ timeOut = timeout;timeOutUnit = unit;return this;}

    public enum ReadReplicateMode {
        FROM_MASTER,
        FROM_REPLICATE,
        FROM_MASTER_THEN_REPLICATE
    }
}

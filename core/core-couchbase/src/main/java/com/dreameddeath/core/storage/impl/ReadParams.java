package com.dreameddeath.core.storage.impl;

import java.util.concurrent.TimeUnit;

/**
 * Created by ceaj8230 on 12/12/2014.
 */
public class ReadParams {
    private ReadReplicateMode _readMode=ReadReplicateMode.FROM_MASTER;
    private long _timeOut=0;
    private TimeUnit _timeOutUnit=null;

    public ReadReplicateMode getReadMode() {
        return _readMode;
    }
    public void setReadMode(ReadReplicateMode readMode) {
        _readMode = readMode;
    }

    public long getTimeOut() {
        return _timeOut;
    }
    public void setTimeOut(long timeOut) {
        _timeOut = timeOut;
    }

    public TimeUnit getTimeOutUnit() {
        return _timeOutUnit;
    }
    public void setTimeOutUnit(TimeUnit timeOutUnit) {
        _timeOutUnit = timeOutUnit;
    }

    public static ReadParams create(){return new ReadParams();}

    public ReadParams with(ReadReplicateMode mode){_readMode = mode;return this;}
    public ReadParams with(long timeout,TimeUnit unit){ _timeOut = timeout;_timeOutUnit = unit;return this;}

    public enum ReadReplicateMode {
        FROM_MASTER,
        FROM_REPLICATE,
        FROM_MASTER_THEN_REPLICATE
    };
}

package com.dreameddeath.core.storage.impl.simulator;

import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

/**
 * Created by CEAJ8230 on 24/11/2014.
 */
public class DocumentSimulator {
    private String _key;
    private long _cas;
    private int _flags;
    private int _expiry;
    private ByteBuf _data;

    public void setKey(String key){ _key = key;}
    public String getKey(){ return _key;}

    public void setCas(long cas){ _cas = cas;}
    public long getCas(){ return _cas;}

    public void setFlags(int flags){ _flags = flags;}
    public int getFlags(){ return _flags;}

    public void setExpiry(int expiry){ _expiry = expiry;}
    public int getExpiry(){ return _expiry;}

    public void setData(ByteBuf data){ _data = data;}
    public ByteBuf getData(){ return _data;}
}

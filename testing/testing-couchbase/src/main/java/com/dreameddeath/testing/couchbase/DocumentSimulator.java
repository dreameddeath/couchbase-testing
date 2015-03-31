package com.dreameddeath.testing.couchbase;


import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Base64;


/**
 * Created by CEAJ8230 on 24/11/2014.
 */
public class DocumentSimulator {
    private final static ScriptEngineManager _enginefactory = new ScriptEngineManager();
    // create a JavaScript engine
    private final static ScriptEngine _engine = _enginefactory.getEngineByName("JavaScript");

    private String _key;
    private long _cas;
    private int _flags;
    private int _expiry;
    private ByteBuf _data;
    private Object _javascriptObject;
    private String _type;

    public void setKey(String key){ _key = key;}
    public String getKey(){ return _key;}

    public void setCas(long cas){ _cas = cas;}
    public long getCas(){ return _cas;}

    public void setFlags(int flags){ _flags = flags;}
    public int getFlags(){ return _flags;}

    public void setExpiry(int expiry){ _expiry = expiry;}
    public int getExpiry(){ return _expiry;}

    public void setData(ByteBuf data){ _data = data; toJavaScriptDoc();}
    public ByteBuf getData(){ return _data;}

    public String getType(){ return _type;}
    public Object getJavascriptObject(){ return _javascriptObject;}

    protected void toJavaScriptDoc(){
        try {
            _javascriptObject= _engine.eval("("+new String(_data.array())+")");
            _type = "json";
        }
        catch(ScriptException e){
            _javascriptObject = Base64.getEncoder().encode(_data.array());
            _type = "base64";
        }
    }

    public Meta getMeta(){ return new Meta(this);}


    public static class Meta{
        public String id;
        public String rev;
        public String type;
        public Integer flags;
        public Integer expiration;

        public Meta(DocumentSimulator doc){
            id = doc.getKey();
            rev = Long.toString(doc.getCas());
            type = doc.getType();
            flags = doc.getFlags();
            expiration = doc.getExpiry();
        }
    }
}

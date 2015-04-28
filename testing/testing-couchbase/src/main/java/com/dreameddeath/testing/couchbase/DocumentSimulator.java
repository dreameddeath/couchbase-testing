/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.testing.couchbase;


import com.couchbase.client.deps.io.netty.buffer.ByteBuf;

import javax.script.ScriptException;
import java.util.Base64;


/**
 * Created by CEAJ8230 on 24/11/2014.
 */
public class DocumentSimulator {
    private CouchbaseBucketSimulator _parent;
    //private final static ScriptEngineManager _enginefactory = new ScriptEngineManager();
    // create a JavaScript engine
    //private final static ScriptEngine _engine = _enginefactory.getEngineByName("JavaScript");


    public DocumentSimulator(CouchbaseBucketSimulator parentSimulator){
        _parent = parentSimulator;
    }

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

    public void setData(ByteBuf data){ _data = data.copy(); toJavaScriptDoc();}
    public void appendData(ByteBuf data){ _data= _data.writeBytes(data);}
    public void prependData(ByteBuf data){_data= data.copy().writeBytes(_data); }
    public ByteBuf getData(){ return _data;}

    public String getType(){ return _type;}
    public Object getJavascriptObject(){ return _javascriptObject;}

    protected void toJavaScriptDoc(){
        try {
            _javascriptObject= _parent.getJavaScriptEngine().eval("("+new String(_data.array())+")");
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

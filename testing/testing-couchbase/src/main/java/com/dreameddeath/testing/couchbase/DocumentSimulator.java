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

package com.dreameddeath.testing.couchbase;


import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;

import javax.script.ScriptException;
import java.nio.charset.Charset;
import java.util.Base64;


/**
 * Created by Christophe Jeunesse on 24/11/2014.
 */
public class DocumentSimulator {
    private CouchbaseBucketSimulator parent;
    //private final static ScriptEngineManager _enginefactory = new ScriptEngineManager();
    // create a JavaScript engine
    //private final static ScriptEngine _engine = _enginefactory.getEngineByName("JavaScript");


    public DocumentSimulator(CouchbaseBucketSimulator parentSimulator){
        parent = parentSimulator;
    }

    private String key;
    private long cas;
    private int flags;
    private int expiry;
    private ByteBuf data;
    private Object javascriptObject;
    private String type;

    public void setKey(String key){ this.key = key;}
    public String getKey(){ return key;}

    public void setCas(long cas){ this.cas = cas;}
    public long getCas(){ return cas;}

    public void setFlags(int flags){ this.flags = flags;}
    public int getFlags(){ return flags;}

    public void setExpiry(int expiry){ this.expiry = expiry;}
    public int getExpiry(){ return expiry;}

    public void setData(ByteBuf data){ this.data = data.copy(); toJavaScriptDoc();}
    public void appendData(ByteBuf data){
        this.data = Unpooled.copiedBuffer(this.data, data);
        toJavaScriptDoc();
    }

    public void prependData(ByteBuf data){
        this.data = Unpooled.copiedBuffer(data,this.data);
        toJavaScriptDoc();
    }

    public ByteBuf getData(){ return data;}

    public String getType(){ return type;}
    public Object getJavascriptObject(){ return javascriptObject;}

    protected void toJavaScriptDoc(){
        try {
            javascriptObject= parent.getJavaScriptEngine().eval("("+new String(data.array(), Charset.forName("utf-8"))+")");
            type = "json";
        }
        catch(ScriptException e){
            javascriptObject = Base64.getEncoder().encode(data.array());
            type = "base64";
        }
    }

    public Meta getMeta(){ return new Meta(this);}


    public static class Meta{
        public final String id;
        public final String rev;
        public final String type;
        public final Integer flags;
        public final Integer expiration;

        public Meta(DocumentSimulator doc){
            id = doc.getKey();
            rev = Long.toString(doc.getCas());
            type = doc.getType();
            flags = doc.getFlags();
            expiration = doc.getExpiry();
        }
    }
}

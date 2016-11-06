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

package com.dreameddeath.core.couchbase.exception;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 13/09/2014.
 */
public class DocumentNotFoundException extends StorageException {
    private final CouchbaseDocument doc;
    private final String key;

    public DocumentNotFoundException(String key,String message){
        super(message);
        this.key=key;
        this.doc=null;
    }

    public DocumentNotFoundException(String key,String message,Throwable e){
        super(message,e);
        this.key=key;
        this.doc=null;
    }

    public DocumentNotFoundException(String key,Throwable e){
        super(e);
        this.key=key;
        this.doc=null;
    }


    public DocumentNotFoundException(CouchbaseDocument doc, String message){
        super(message);
        this.doc=doc;
        this.key = doc.getBaseMeta().getKey();
    }

    public DocumentNotFoundException(CouchbaseDocument doc, Throwable e){
        super(e);
        this.doc=doc;
        this.key = doc.getBaseMeta().getKey();
    }

    public DocumentNotFoundException(CouchbaseDocument doc, String message,Throwable e){
        super(message,e);
        this.doc=doc;
        this.key = doc.getBaseMeta().getKey();
    }

    @Override
    public String getMessage(){
        StringBuilder builder = new StringBuilder(super.getMessage());
        if(doc!=null){ builder.append(" The doc was <").append(doc).append(">");}
        if(key!=null){ builder.append(" The key was <").append(key).append(">");}
        return builder.toString();
    }

    public String getKey(){
        return key;
    }
}

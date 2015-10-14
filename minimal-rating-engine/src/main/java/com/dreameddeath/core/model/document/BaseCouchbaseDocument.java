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

package com.dreameddeath.core.model.document;

import com.dreameddeath.core.CouchbaseSession;
import com.dreameddeath.core.couchbase.CouchbaseConstants;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Christophe Jeunesse on 11/09/2014.
 */
public class BaseCouchbaseDocument extends BaseCouchbaseDocumentElement {
    private BaseMetaInfo meta;

    public BaseMetaInfo getBaseMeta(){return meta;}
    public void setBaseMeta(BaseMetaInfo meta){ this.meta=meta;}

    public BaseCouchbaseDocument(){meta=this.new BaseMetaInfo();}
    public BaseCouchbaseDocument(BaseMetaInfo meta){this.meta=meta;}

    public class BaseMetaInfo {
        private CouchbaseSession session;
        private String key;
        private long   cas;
        private Boolean isLocked;
        private Integer dbDocSize;
        private Collection<CouchbaseConstants.DocumentFlag> flags =new HashSet<CouchbaseConstants.DocumentFlag>();
        private int expiry;
        private DocumentState docState = DocumentState.NEW;

        public final CouchbaseSession getSession(){ return session; }
        public final void setSession(CouchbaseSession session){ this.session = session; }

        public final String getKey(){ return key; }
        public final void setKey(String key){ this.key=key;}

        public final long getCas(){ return cas; }
        public final void setCas(long cas){ this.cas = cas; }

        public final Boolean getIsLocked(){ return isLocked; }
        public final void setIsLocked(Boolean isLocked){ this.isLocked = isLocked; }

        public final Integer getDbSize(){ return dbDocSize; }
        public final void setDbSize(Integer docSize){ this.dbDocSize = docSize; }

        public final Collection<CouchbaseConstants.DocumentFlag> getFlags(){ return flags; }
        public final Integer getEncodedFlags(){ return CouchbaseConstants.DocumentFlag.pack(flags); }
        public final void setEncodedFlags(Integer encodedFlags){ flags.clear(); flags.addAll(CouchbaseConstants.DocumentFlag.unPack(encodedFlags)); }
        public final void setFlags(Collection<CouchbaseConstants.DocumentFlag> flags){ flags.clear(); flags.addAll(flags); }
        public final void addEncodedFlags(Integer encodedFlags){ flags.addAll(CouchbaseConstants.DocumentFlag.unPack(encodedFlags)); }
        public final void addFlag(CouchbaseConstants.DocumentFlag flag){ flags.add(flag); }
        public final void addFlags(Collection<CouchbaseConstants.DocumentFlag> flags){ flags.addAll(flags); }
        public final void removeFlag(CouchbaseConstants.DocumentFlag flag){ flags.remove(flag); }
        public final void removeFlags(Collection<CouchbaseConstants.DocumentFlag> flags){flags.remove(flags); }
        public boolean hasFlag(CouchbaseConstants.DocumentFlag flag){ return flags.contains(flag); }


        public int getExpiry(){return expiry;}
        public void setExpiry(int expiry){ this.expiry=expiry;}

        public void setStateDirty(){
            if(docState.equals(DocumentState.SYNC)){
                docState = DocumentState.DIRTY;
            }
        }

        public void setStateDeleted(){
            docState = DocumentState.DELETED;
        }

        public void setStateSync(){ docState = DocumentState.SYNC; }
        public DocumentState getState(){ return docState; }



        @Override
        public String toString(){
            return
                    "key   : "+key+",\n"+
                    "cas   : "+cas+",\n"+
                    "lock  : "+isLocked+",\n"+
                    "size  : "+dbDocSize+",\n"+
                    "state : "+docState +",\n"+
                    "flags : "+ flags.toString();
        }

    }

    public boolean equals(BaseCouchbaseDocument doc){
        if     (doc == null){ return false;}
        else if(doc == this){ return true; }
        else if(meta.getKey()!=null) { return meta.getKey().equals(doc.meta.getKey()); }
        else                { return false; }
    }

    public enum DocumentState {
        NEW,
        DIRTY,
        SYNC,
        DELETED
    }

}

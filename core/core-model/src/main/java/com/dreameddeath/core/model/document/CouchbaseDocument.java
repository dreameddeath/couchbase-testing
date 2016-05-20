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


import com.dreameddeath.core.model.property.HasParent;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 11/09/2014.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,fieldVisibility= JsonAutoDetect.Visibility.NONE,isGetterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class CouchbaseDocument implements HasParent {
    private BaseMetaInfo meta;

    public BaseMetaInfo getBaseMeta(){return meta;}
    public void setBaseMeta(BaseMetaInfo meta){ this.meta=meta;}

    public CouchbaseDocument(){meta=this.new BaseMetaInfo();}
    public CouchbaseDocument(BaseMetaInfo meta){this.meta=meta;}

    public class BaseMetaInfo {
        //private CouchbaseSession _session;
        private String key;
        private long   cas;
        private Boolean isLocked;
        private Integer dbDocSize;
        private long vbucketID=0;
        private long vbucketUUID=0;
        private long sequenceNumber=0;

        private Set<DocumentFlag> flags =EnumSet.noneOf(DocumentFlag.class);
        private int expiry;
        private DocumentState docState = DocumentState.NEW;
        private boolean isFrozen = false;

        public final String getKey(){ return key; }
        public final void setKey(String key){ this.key=key;}

        public final long getCas(){ return cas; }
        public final void setCas(long cas){ this.cas = cas; }

        public final Boolean getIsLocked(){ return isLocked; }
        public final void setIsLocked(Boolean isLocked){ this.isLocked = isLocked; }

        public final Integer getDbSize(){ return dbDocSize; }
        public final void setDbSize(Integer docSize){ this.dbDocSize = docSize; }

        public final Collection<DocumentFlag> getFlags(){ return flags; }
        public final Integer getEncodedFlags(){ return DocumentFlag.pack(flags); }
        public final void setEncodedFlags(Integer encodedFlags){ flags.clear(); flags.addAll(DocumentFlag.unPack(encodedFlags)); }
        public final void setFlags(Collection<DocumentFlag> flags){ flags.clear(); flags.addAll(flags); }
        public final void addEncodedFlags(Integer encodedFlags){ flags.addAll(DocumentFlag.unPack(encodedFlags)); }
        public final void addFlag(DocumentFlag flag){ flags.add(flag); }
        public final void addFlags(Collection<DocumentFlag> flags){ flags.addAll(flags); }
        public final void removeFlag(DocumentFlag flag){ flags.remove(flag); }
        public final void removeFlags(Collection<DocumentFlag> flags){flags.remove(flags); }
        public boolean hasFlag(DocumentFlag flag){ return flags.contains(flag); }


        public int getExpiry(){return expiry;}
        public void setExpiry(int expiry){ this.expiry=expiry;}

        public void setStateDirty(){
            checkFrozen();
            if(docState.equals(DocumentState.SYNC)){
                docState = DocumentState.DIRTY;
            }
        }

        public void setStateDeleted(){
            checkFrozen();
            docState = DocumentState.DELETED;
        }

        public void setStateSync(){ docState = DocumentState.SYNC; }
        public DocumentState getState(){ return docState; }

        public long getVbucketID() {
            return vbucketID;
        }

        public void setVbucketID(long vbucketID) {
            this.vbucketID = vbucketID;
        }

        public long getVbucketUUID() {
            return vbucketUUID;
        }

        public void setVbucketUUID(long vbucketUUID) {
            this.vbucketUUID = vbucketUUID;
        }

        public long getSequenceNumber() {
            return sequenceNumber;
        }

        public void setSequenceNumber(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }

        @Override
        public String toString(){
            return
                    "key   : "+key+",\n"+
                    "cas   : "+cas+",\n"+
                    "mut   : "+vbucketID+"#"+vbucketUUID+"#"+sequenceNumber+",\n"+
                    "lock  : "+isLocked+",\n"+
                    "size  : "+dbDocSize+",\n"+
                    "state : "+docState +",\n"+
                    "flags : "+flags.toString();
        }

        public void freeze(){
            this.isFrozen = true;
        }

        public void unfreeze(){
            this.isFrozen = false;
        }

        public boolean isFrozen() {
            return isFrozen;
        }

        private final void checkFrozen(){
            if(isFrozen){
                throw new IllegalStateException("The modification on the document isn't allowed");
            }
        }
    }

    public boolean equals(CouchbaseDocument doc){
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

    public enum DocumentFlag {
        Binary(0x01),
        Compressed(0x100),
        Deleted(0x200);

        private int value;

        DocumentFlag(int value){
            this.value = value;
        }

        public int toInteger(){
            return value;
        }

        @Override
        public String toString(){
            return String.format("%s(0x%X)",super.toString(),value);
        }

        static public Set<DocumentFlag> unPack(int binValue){
            Set<DocumentFlag> result=new HashSet<DocumentFlag>();
            for(DocumentFlag flag:DocumentFlag.values()){
                if((flag.value & binValue)!=0){
                    result.add(flag);
                }
            }
            return result;
        }

        static public int pack(Collection<DocumentFlag> flags){
            int result = 0;
            for(DocumentFlag flag :flags){
                result|= flag.toInteger();
            }
            return result;
        }
    }

    @Override
    public HasParent getParentElement(){return null;}
    @Override
    public void setParentElement(HasParent parent){throw new UnsupportedOperationException();}
}

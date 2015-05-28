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
    private BaseMetaInfo _meta;

    public BaseMetaInfo getBaseMeta(){return _meta;}
    public void setBaseMeta(BaseMetaInfo meta){ _meta=meta;}

    public CouchbaseDocument(){_meta=this.new BaseMetaInfo();}
    public CouchbaseDocument(BaseMetaInfo meta){_meta=meta;}

    public class BaseMetaInfo {
        //private CouchbaseSession _session;
        private String _key;
        private long   _cas;
        private Boolean _isLocked;
        private Integer _dbDocSize;
        private Set<DocumentFlag> _flags =EnumSet.noneOf(DocumentFlag.class);
        private int _expiry;
        private DocumentState _docState = DocumentState.NEW;

        public final String getKey(){ return _key; }
        public final void setKey(String key){ _key=key;}

        public final long getCas(){ return _cas; }
        public final void setCas(long cas){ this._cas = cas; }

        public final Boolean getIsLocked(){ return _isLocked; }
        public final void setIsLocked(Boolean isLocked){ this._isLocked = isLocked; }

        public final Integer getDbSize(){ return _dbDocSize; }
        public final void setDbSize(Integer docSize){ this._dbDocSize = docSize; }

        public final Collection<DocumentFlag> getFlags(){ return _flags; }
        public final Integer getEncodedFlags(){ return DocumentFlag.pack(_flags); }
        public final void setEncodedFlags(Integer encodedFlags){ _flags.clear(); _flags.addAll(DocumentFlag.unPack(encodedFlags)); }
        public final void setFlags(Collection<DocumentFlag> flags){ _flags.clear(); _flags.addAll(flags); }
        public final void addEncodedFlags(Integer encodedFlags){ _flags.addAll(DocumentFlag.unPack(encodedFlags)); }
        public final void addFlag(DocumentFlag flag){ _flags.add(flag); }
        public final void addFlags(Collection<DocumentFlag> flags){ _flags.addAll(flags); }
        public final void removeFlag(DocumentFlag flag){ _flags.remove(flag); }
        public final void removeFlags(Collection<DocumentFlag> flags){_flags.remove(flags); }
        public boolean hasFlag(DocumentFlag flag){ return _flags.contains(flag); }


        public int getExpiry(){return _expiry;}
        public void setExpiry(int expiry){ _expiry=expiry;}

        public void setStateDirty(){
            if(_docState.equals(DocumentState.SYNC)){
                _docState = DocumentState.DIRTY;
            }
        }

        public void setStateDeleted(){
            _docState = DocumentState.DELETED;
        }

        public void setStateSync(){ _docState = DocumentState.SYNC; }
        public DocumentState getState(){ return _docState; }



        @Override
        public String toString(){
            return
                    "key   : "+_key+",\n"+
                    "cas   : "+_cas+",\n"+
                    "lock  : "+_isLocked+",\n"+
                    "size  : "+_dbDocSize+",\n"+
                    "state : "+_docState +",\n"+
                    "flags : "+_flags.toString();
        }

    }

    public boolean equals(CouchbaseDocument doc){
        if     (doc == null){ return false;}
        else if(doc == this){ return true; }
        else if(_meta.getKey()!=null) { return _meta.getKey().equals(doc._meta.getKey()); }
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

        private int _value;

        DocumentFlag(int value){
            this._value = value;
        }

        public int toInteger(){
            return _value;
        }

        @Override
        public String toString(){
            return String.format("%s(0x%X)",super.toString(),_value);
        }

        static public Set<DocumentFlag> unPack(int binValue){
            Set<DocumentFlag> result=new HashSet<DocumentFlag>();
            for(DocumentFlag flag:DocumentFlag.values()){
                if((flag._value & binValue)!=0){
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

/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.model.document;


import com.dreameddeath.core.model.property.HasParent;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Christophe Jeunesse on 11/09/2014.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,fieldVisibility= JsonAutoDetect.Visibility.NONE,isGetterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class CouchbaseDocument implements HasParent,HasBaseMeta {
    private CouchbaseDocumentBaseMetaInfo meta;

    public CouchbaseDocumentBaseMetaInfo getBaseMeta(){
        return meta;
    }

    public void setBaseMeta(CouchbaseDocumentBaseMetaInfo meta){
        this.meta=meta;
    }

    public CouchbaseDocument(){
        meta= new CouchbaseDocumentBaseMetaInfo();
    }

    public CouchbaseDocument(CouchbaseDocumentBaseMetaInfo meta){
        this.meta=meta;
    }

    //@Override
    public boolean isSameDocument(CouchbaseDocument doc){
        if     (doc == null){ return false;}
        else if(doc == this){ return true; }
        else if(meta.getKey()!=null) { return meta.getKey().equals(doc.meta.getKey()); }
        else                { return false; }
    }

    @Override
    public HasParent getParentElement(){
        return null;
    }

    @Override
    public void setParentElement(HasParent parent){
        throw new UnsupportedOperationException();
    }
}

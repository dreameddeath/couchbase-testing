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

package com.dreameddeath.core.business.model;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.google.common.base.Preconditions;
import rx.Observable;

public abstract class BusinessDocumentLink<T extends CouchbaseDocument> extends CouchbaseDocumentElement {
    private volatile T docTempObject;
    @DocumentProperty("key") @NotNull
    private final Property<String> key=new ImmutableProperty<>(BusinessDocumentLink.this);

    public final String getKey(){
        String keyVal = key.get();
        if(keyVal==null){
            synchronized (this) {
                keyVal=key.get();
                if(keyVal==null) {
                    keyVal = docTempObject.getBaseMeta().getKey();
                    if (keyVal != null) {
                        key.set(keyVal);
                    }
                }
            }
        }
        return keyVal;
    }

    public final void setKey(String key){
        Preconditions.checkNotNull(key);
        this.key.set(key);
    }

    public Observable<T> getLinkedObject(ICouchbaseSession session){
        if(key!=null) {
            return session.asyncGet(key.get());
        }
        else{
            return Observable.just(docTempObject);
        }
    }

    private void setTemporaryLinkedObject(T docObj){
        Preconditions.checkArgument(getKey()==null);
        Preconditions.checkNotNull(docObj);
        Preconditions.checkArgument(docObj.getBaseMeta().getKey()==null);
        docTempObject=docObj;
    }

    public boolean isLinkTo(T docObj){
        return isLinkTo(docObj.getBaseMeta().getKey());
    }

    public boolean isLinkTo(String lookupKey){
        String key=getKey();
        return key!=null && key.equals(lookupKey);
    }

    public BusinessDocumentLink(){}

    public BusinessDocumentLink(T targetDoc){
        if(targetDoc.getBaseMeta().getKey()!=null){
            setKey(targetDoc.getBaseMeta().getKey());
        }
        else {
            setTemporaryLinkedObject(targetDoc);
        }
    }
    
    public BusinessDocumentLink(BusinessDocumentLink<T> srcLink){
        T srcDoc = srcLink.docTempObject;
        String srcKey=srcLink.getKey();
        if(srcKey!=null) {
            setKey(srcKey);
        }
        else {
            setTemporaryLinkedObject(srcDoc);
        }
    }
    
    
    @Override
    public boolean equals(Object target){
        if(target==null){
            return false;
        }
        else if(this == target){
            return true;
        }
        else if(target instanceof BusinessDocumentLink){
            BusinessDocumentLink<CouchbaseDocument> targetLnk=(BusinessDocumentLink<CouchbaseDocument>) target;
            String currKey=getKey();
            String targetKey=getKey();
            if(currKey!=null && currKey.equals(targetKey)){
                return true;
            }
            else if(docTempObject!=null && docTempObject.isSameDocument(targetLnk.docTempObject)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = docTempObject != null ? docTempObject.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }

    @Override
    public String toString(){
        return "key : "+getKey();
    }
}

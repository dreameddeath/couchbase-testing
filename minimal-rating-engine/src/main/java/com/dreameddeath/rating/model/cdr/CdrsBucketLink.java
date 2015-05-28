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

package com.dreameddeath.rating.model.cdr;


import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.binary.BinaryCouchbaseDocument;
import com.dreameddeath.core.model.business.CouchbaseDocumentLink;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

public class CdrsBucketLink<T extends GenericCdrsBucket> extends CouchbaseDocumentLink<T>{
    @DocumentProperty("@c")
    private Property<String> _class= new StandardProperty<String>(CdrsBucketLink.this);
    @DocumentProperty("nbCdrs")
    private Property<Integer>   _nbCdrs= new StandardProperty<Integer>(CdrsBucketLink.this,0);
    @DocumentProperty("dbSize")
    private Property<Integer>  _dbSize= new StandardProperty<Integer>(CdrsBucketLink.this,0);
    
    public String getType() { return _class.get();}
    public void setType(String clazz) { _class.set(clazz);}
    
    public Integer getNbCdrs() { return _nbCdrs.get();}
    public void setNbCdrs(Integer nbCdrs) { _nbCdrs.set(nbCdrs);}
    
    public Integer getDbSize() { return _dbSize.get();}
    public void setDbSize(Integer dbSize) { _dbSize.set(dbSize);}
    
    public CdrsBucketLink(){}
    public CdrsBucketLink(T bucket){
        super(bucket);
        setType(bucket.getClass().getSimpleName());
        setNbCdrs(bucket.getCdrs().size());
        setDbSize(bucket.getBinaryMeta().getLastWrittenSize());
    }
    
    public CdrsBucketLink(CdrsBucketLink srcLink){
        super(srcLink);
        setType(srcLink.getType());
        setNbCdrs(srcLink.getNbCdrs());
        setDbSize(srcLink.getDbSize());
    }
    
    public void updateFromBucket(T bucketUpdate) {
        if(bucketUpdate.getBinaryMeta().getBinaryDocumentType().equals(BinaryCouchbaseDocument.BinaryDocumentType.BINARY_FULL)){
            _nbCdrs.set(bucketUpdate.getCdrs().size());
            _dbSize.set(bucketUpdate.getBinaryMeta().getLastWrittenSize());
        }
        else{
            _nbCdrs.set(_nbCdrs.get()+bucketUpdate.getCdrs().size());
            _dbSize.set(_dbSize.get()+bucketUpdate.getBinaryMeta().getLastWrittenSize());
        }
    }
    
    
    @Override
    public String toString(){
        String result ="{\n"+super.toString()+",\n";
        result+="type : "+getType();
        result+="}\n";
        return result;
    }
    
}
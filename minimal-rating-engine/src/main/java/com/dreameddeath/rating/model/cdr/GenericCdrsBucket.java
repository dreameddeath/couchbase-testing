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

package com.dreameddeath.rating.model.cdr;

import com.dreameddeath.core.model.binary.BinaryCouchbaseDocument;
import com.dreameddeath.core.storage.CouchbaseConstants;

import java.util.*;
/**
*  This abstract class is use to manage a bucket of CDRs :
*  - it have to keep the original order of cdrs
*  - the CDRs can be searched from the unique id of the CDRs
*  - it keeps the last appended size
*  - the type will be used to manage the storage in append mode
*
*  The cdr itself is managed by the @see GenericCdr class.
*/
public abstract class GenericCdrsBucket<T extends GenericCdr> extends BinaryCouchbaseDocument {
    ///The key of the parent billing account
    private String _billingAccountKey;
    ///The key of the parent billing cycle
    private String _billingCycleKey;
    ///The key of the parent rating Context
    private String _ratingContextKey;
    
    /// List of CDRs in the database order
    private List<T> _cdrs=new ArrayList<T>();
    /// Map of the uid to CDRs to allow search of a CDR from the unique id
    private Map<String,T> _cdrsMap = new HashMap<String,T>();
    
    /**
    * Standard constructor 
    * @param documentType It has to be carefully chosen.
    *        if type == BINARY_FULL, the object contains the whole bucket
    *        if type == BINARY_PARTIAL_WITHOUT_CHECKSUM, the object will be used to normally a CDR to be rating
    *        if type == BINARY_PARTIAL_WITH_CHECKSUM, the object will be used to append a rated CDR
    */
    public GenericCdrsBucket(BinaryDocumentType documentType) {
        super(documentType);
        getBinaryMeta().addFlag(CouchbaseConstants.DocumentFlag.CdrBucket);
    }
    
    /**
    * Incremental rating constructor 
    * @param origDbSize the database size prior to the appending of the cdr
    * @param documentType It has to be carefully chosen.
    *        if type == BINARY_BUCKET_FULL, it shouldn't be used (add this constructor is more designed to be a delta mode)
    *        if type == BINARY_BUCKET_PARTIAL_WITHOUT_CHECKSUM, the object will be used to normally add CDR to the rating
    *        if type == BINARY_BUCKET_PARTIAL_WITH_CHECKSUM, the object will be used to append a rated CDR
    */
    public GenericCdrsBucket(Integer origDbSize,BinaryDocumentType documentType){
        super(origDbSize,documentType);
    }
    
    /// Billing Account Key Getter/Setter
    public String getBillingAccountKey(){ return _billingAccountKey;}
    public void setBillingAccountKey(String baKey){_billingAccountKey=baKey;}
    
    /// Billing Account Key Getter/Setter
    public String getBillingCycleKey(){ return _billingCycleKey;}
    public void setBillingCycleKey(String cycleKey){_billingCycleKey=cycleKey;}
    
    /// Rating Context Account Key Getter/Setter
    public String getRatingContextKey(){ return _ratingContextKey;}
    public void setRatingContextKey(String ratingCtxtKey){_ratingContextKey=ratingCtxtKey;}



    public int getGlobalOverheadCounter(){
        int result=0;
        for(T cdr:_cdrs){
            result+=cdr.getOverheadCounter();
        }
        return result;
    }
    
    /// Retrieve a CDR from its key (unique id)
    public T getCdrFromKey(String key){ return _cdrsMap.get(key); }
    /// Tell if a given Unique Cdr id is existing
    public boolean isCdrExisting(String key){ return _cdrsMap.containsKey(key); }
    /// Get the equivalent cdr (same unique id)
    public T getCdr(T cdr){ return getCdrFromKey(cdr.getUid());}
    
    
    /// Add a CDR to the list (with cleanup if existing
    public void addCdr(T cdr){
        removeCdr(cdr);
        _cdrs.add(cdr);
        _cdrsMap.put(cdr.getUid(),cdr);
    }
    
    public void removeCdr(T cdr){
        if(cdr!=null){
            T realCdr = _cdrsMap.remove(cdr.getUid());
            _cdrs.remove(realCdr);
        }
    }
    
    public void removeCdr(String key){
        removeCdr(_cdrsMap.get(key));
    }
    
    public void addAllCdrs(Collection<T> cdrsList){
        for(T cdr:cdrsList){
            removeCdr(cdr);//Clean existing key if exists
            addCdr(cdr);
        }
    }
    
    public void removeAllCdrs(Collection<T> cdrsList){
        for(T cdr:cdrsList){
            removeCdr(cdr);
        }
    }
    
    public void removeAllCdrsFromKeys(Collection<String> keys){
        for(String key:keys){
            removeCdr(key);
        }
    }
    
    
    ///In order Cdrs Accessors
    public List<T> getCdrs(){
        return Collections.unmodifiableList(_cdrs);
    }
    
    ///Map type Cdrs Accessors
    public Map<String,T> getCdrsMap(){
        return Collections.unmodifiableMap(_cdrsMap);
    }
    
    @Override
    public String toString(){
        return 
            "{\n"+
            "    "+super.toString()+",\n"+
            "    ba : "+_billingAccountKey+",\n"+
            "    cycle : "+_billingCycleKey+",\n"+
            "    ctxt : "+_ratingContextKey+",\n"+
            "    cdrs : \n"+
            "    "+_cdrs.toString()+"\n"+
            "    \n"+
            "}\n";
    }
}
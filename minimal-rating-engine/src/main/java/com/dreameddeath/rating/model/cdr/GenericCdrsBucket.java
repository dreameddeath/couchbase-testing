package com.dreameddeath.rating.model.cdr;

import com.dreameddeath.core.model.document.CouchbaseDocument;
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
public abstract class GenericCdrsBucket<T extends GenericCdr> extends CouchbaseDocument{
    ///The key of the parent billing account
    private String _billingAccountKey;
    ///The key of the parent billing cycle
    private String _billingCycleKey;
    ///The key of the parent rating Context
    private String _ratingContextKey;
    
    /// The document type is used during the Transcoder
    private DocumentType _cdrBucketDocumentType;
    /// The check-sum of the last cdrs read to detect the error
    private int _endingCheckSum;
    /// The last append/written size
    private int _lastWrittenSize;
    /// List of CDRs in the database order
    private List<T> _cdrs=new ArrayList<T>();
    /// Map of the uid to CDRs to allow search of a CDR from the unique id
    private Map<String,T> _cdrsMap = new HashMap<String,T>();
    
    /**
    * Standard constructor 
    * @param documentType It has to be carefully chosen.
    *        if type == CDRS_BUCKET_FULL, the object contains the whole bucket
    *        if type == CDRS_BUCKET_PARTIAL_WITHOUT_CHECKSUM, the object will be used to normally a CDR to be rating
    *        if type == CDRS_BUCKET_PARTIAL_WITH_CHECKSUM, the object will be used to append a rated CDR
    */
    public GenericCdrsBucket(DocumentType documentType){
        _cdrBucketDocumentType = documentType;
        _endingCheckSum = 0;
        _lastWrittenSize = 0;
        addDocumentFlag(CouchbaseConstants.DocumentFlag.CdrBucket);
    }
    
    
    /**
    * Incremental rating constructor 
    * @param key the key of the CDR bucket
    * @param origDbSize the database size prior to the appending of the cdr
    * @param documentType It has to be carefully chosen.
    *        if type == CDRS_BUCKET_FULL, it shouldn't be used (add this constructor is more designed to be a delta mode)
    *        if type == CDRS_BUCKET_PARTIAL_WITHOUT_CHECKSUM, the object will be used to normally add CDR to the rating
    *        if type == CDRS_BUCKET_PARTIAL_WITH_CHECKSUM, the object will be used to append a rated CDR
    */
    public GenericCdrsBucket(String key,Integer origDbSize,DocumentType documentType){
        this(documentType);
        setDocumentDbSize(origDbSize);
        setDocumentKey(key);
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
    
    /// Checksum Getter/Setter
    public int getEndingCheckSum(){ return _endingCheckSum;}
    public void setEndingCheckSum(int endingCheckSum){_endingCheckSum=endingCheckSum;}
    
    
    /// Last Written Size Getter/Setter
    public int getLastWrittenSize(){return _lastWrittenSize;}
    public void setLastWrittenSize(int appendedSize){_lastWrittenSize=appendedSize;}
    
    /// Getter of document Type
    public DocumentType getCdrBucketDocumentType(){return _cdrBucketDocumentType; }
    
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
    
    /**
    *  CDR Bucket Document Types
    */
    public static enum DocumentType{
        CDRS_BUCKET_FULL("full"),
        CDRS_BUCKET_PARTIAL_WITH_CHECKSUM("partial_with_checksum"),
        CDRS_BUCKET_PARTIAL_WITHOUT_CHECKSUM("partiel_without_checksum");
        
        
        private String _value;
        
        DocumentType(String value){
            this._value = value;
        }
        
        @Override
        public String toString(){
            return _value;
        }
    }
    
}
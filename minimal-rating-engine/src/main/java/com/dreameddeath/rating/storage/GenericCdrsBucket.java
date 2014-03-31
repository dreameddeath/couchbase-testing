package com.dreameddeath.rating.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

import com.dreameddeath.common.storage.CouchbaseConstants;
import com.dreameddeath.common.storage.CouchbaseDocument;

public abstract class GenericCdrsBucket<T extends GenericCdr> extends CouchbaseDocument{//extends HashMap<String,T>{
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
    
    
    
    private DocumentType _cdrBucketDocumentType;
    private int _checkSum;
    private List<T> _cdrs=new ArrayList<T>();
    private Map<String,T> _cdrsMap = new HashMap<String,T>();
    
    
    //Incremental rating constructor
    public GenericCdrsBucket(String key,Integer origDbSize,DocumentType documentType){
        this(documentType);
        setDbDocSize(origDbSize);
        setKey(key);
    }
    
    
    
    public GenericCdrsBucket(DocumentType documentType){
        this._cdrBucketDocumentType = documentType;
        addDocumentFlag(CouchbaseConstants.DocumentFlag.CdrBucket);
    }
    
    public DocumentType getCdrBucketDocumentType(){
        return this._cdrBucketDocumentType;
    }
    
    public int getGlobalOverheadCounter(){
        int result=0;
        for(T cdr:_cdrs){
            result+=cdr.getOverheadCounter();
        }
        return result;
    }
    
    public T getCdrFromKey(String key){
        return _cdrsMap.get(key);
    }
    
    public boolean isCdrExisting(String key){
        return _cdrsMap.containsKey(key);
    }
    
    public T getCdr(T cdr){
        return getCdrFromKey(cdr.getUid());
    }
    
    
    public void addCdr(T cdr){
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
    
    
    public List<T> getCdrs(){
        return Collections.unmodifiableList(_cdrs);
    }
    
    public Map<String,T> getCdrsMap(){
        return Collections.unmodifiableMap(_cdrsMap);
    }
    
    @Override
    public String toString(){
        return 
            "{\n"+
            "    "+super.toString()+"\n"+
            "    cdrs : [\n"+
            "    "+_cdrs.toString()+"\n"+
            "    ]\n"+
            "}\n";
    }
    
}
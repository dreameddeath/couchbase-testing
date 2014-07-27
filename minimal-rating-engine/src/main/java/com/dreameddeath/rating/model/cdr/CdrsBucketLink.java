package com.dreameddeath.rating.model.cdr;


import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocumentLink;
import com.dreameddeath.common.model.property.StandardProperty;
import com.dreameddeath.common.model.property.Property;
import com.dreameddeath.rating.storage.GenericCdrsBucket;

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
        setDbSize(bucket.getLastWrittenSize());
    }
    
    public CdrsBucketLink(CdrsBucketLink srcLink){
        super(srcLink);
        setType(srcLink.getType());
        setNbCdrs(srcLink.getNbCdrs());
        setDbSize(srcLink.getDbSize());
    }
    
    public void updateFromBucket(T bucketUpdate) {
        if(bucketUpdate.getCdrBucketDocumentType().equals(GenericCdrsBucket.DocumentType.CDRS_BUCKET_FULL)){
            _nbCdrs.set(bucketUpdate.getCdrs().size());
            _dbSize.set(bucketUpdate.getLastWrittenSize());
        }
        else{
            _nbCdrs.set(_nbCdrs.get()+bucketUpdate.getCdrs().size());
            _dbSize.set(_dbSize.get()+bucketUpdate.getLastWrittenSize());
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
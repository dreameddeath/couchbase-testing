package com.dreameddeath.rating.model.cdr;


import com.dreameddeath.common.model.CouchbaseDocumentLink;
import com.dreameddeath.rating.storage.GenericCdrsBucket;
import com.dreameddeath.common.annotation.DocumentProperty;

public class CdrsBucketLink<T extends GenericCdrsBucket> extends CouchbaseDocumentLink<T>{
    @DocumentProperty("@c")
    private String _class;
    @DocumentProperty("nbCdrs")
    private Integer   _nbCdrs;
    @DocumentProperty("dbSize")
    private Integer   _dbSize;
    
    public String getType() { return _class;}
    public void setType(String clazz) { _class=clazz;}
    
    public Integer getNbCdrs() { return _nbCdrs;}
    public void setNbCdrs(Integer nbCdrs) { _nbCdrs=nbCdrs;}
    
    public Integer getDbSize() { return _dbSize;}
    public void setDbSize(Integer dbSize) { _dbSize=dbSize;}
    
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
            _nbCdrs=bucketUpdate.getCdrs().size();
            _dbSize=bucketUpdate.getLastWrittenSize();
        }
        else{
            _nbCdrs+=bucketUpdate.getCdrs().size();
            _dbSize+=bucketUpdate.getLastWrittenSize();
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
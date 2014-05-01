package com.dreameddeath.rating.model.cdr;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dreameddeath.common.model.CouchbaseDocumentLink;
import com.dreameddeath.common.model.Property;
import com.dreameddeath.common.model.SynchronizedLinkProperty;
import com.dreameddeath.rating.storage.GenericCdrsBucket;

public class CdrsBucketLink<T extends GenericCdrsBucket> extends CouchbaseDocumentLink<T>{
    private String _class;
    private Integer   _nbCdrs;
    private Integer   _dbSize;
    
    @JsonProperty("@c")
    public String getType() { return _class;}
    public void setType(String clazz) { _class=clazz;}
    
    @JsonProperty("nbCdrs")
    public Integer getNbCdrs() { return _nbCdrs;}
    public void setNbCdrs(Integer nbCdrs) { _nbCdrs=nbCdrs;}
    
    @JsonProperty("dbSize")
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
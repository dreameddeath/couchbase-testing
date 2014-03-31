package com.dreameddeath.rating.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.io.Externalizable;


import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.CachedData;


import com.dreameddeath.common.storage.CouchbaseConstants;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.NormalCdr; 
import com.dreameddeath.rating.storage.ActiveCdrsProtos.PartialCdrRecord;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.OverallCdrsMessage;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.NormalCdrsAppender;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.PartialCdrsAppender;

/**
*  Class used to perform storage preparation based on Raw Cdr Storage Managed (array of bytes)
*  Perform a checksum calculation (length) to allow rating request detection through couchbase request
*  The only contraint it that the Cdrs must have a unique id
*/
public abstract class GenericCdrsBucketTranscoder<T extends GenericCdr> implements Transcoder<GenericCdrsBucket<T>>{
    abstract protected T genericCdrBuilder(String uid);
    abstract protected GenericCdrsBucket<T> genericCdrBucketBuilder(GenericCdrsBucket.DocumentType docType);
    
    
    @Override
    public int getMaxSize(){
        return CachedData.MAX_SIZE;
    }
    
    @Override
    public boolean asyncDecode(CachedData cachedData){
        return false;
    }
    
    
    @Override
    public GenericCdrsBucket<T> decode(CachedData cachedData){
        GenericCdrsBucket<T> result = genericCdrBucketBuilder(GenericCdrsBucket.DocumentType.CDRS_BUCKET_FULL);
        result.addDocumentEncodedFlags(cachedData.getFlags());
        result.setDbDocSize(cachedData.getData().length);
        try{
            unpackStorageDocument(result,cachedData.getData());
        }
        catch(InvalidProtocolBufferException e){
            return null;
        }
        return result;
    }
    
    
    @Override
    public CachedData encode(GenericCdrsBucket<T> input){
        return new CachedData(input.getDocumentEncodedFlags(),packStorageDocument(input),CachedData.MAX_SIZE);
    }
    


    /**
    *   Unpack a message containing Cdrs
    *   @param cdrsBucket the resulting content
    *   @param message the message to unpack
    *   @throws InvalidProtocolBufferException when the message isn't well formatted
    *   @return a list of GenericCdr for successfull found cdrs
    */
    private  void unpackStorageDocument(GenericCdrsBucket<T> cdrsBucket, byte[] message) throws InvalidProtocolBufferException{
        //Unpack Cdrs
        OverallCdrsMessage unpackedMessage = OverallCdrsMessage.parseFrom(message);
        
        //Normal Cdrs uncompress and fill-up Hash Map
        for(NormalCdr unpackedCdr :unpackedMessage.getNormalCdrsList()){
            T foundCdr=genericCdrBuilder(unpackedCdr.getUid());
            foundCdr.setCdrDataSerialized(unpackedCdr.getRawData().toByteArray());
            if(unpackedCdr.getRatingResultsCount()>0){
                for(ByteString ratingResult:unpackedCdr.getRatingResultsList()){
                    foundCdr.addRatingResultSerialized(ratingResult.toByteArray());
                }
            }
            if(unpackedCdr.hasIsDuplicated()){
                foundCdr.setDuplicated(unpackedCdr.getIsDuplicated());
            }
            if(unpackedCdr.hasIsDiscarded()){
                foundCdr.setDiscarded(unpackedCdr.getIsDiscarded());
            }
            
            ///TODO duplicate management exception
            cdrsBucket.addCdr(foundCdr);
        }
        
        //Additional Rating unpacking (rating and duplicate checks)
        for(PartialCdrRecord unpackedRatingResult:unpackedMessage.getPartialResultCdrsList()){
            T foundCdr = cdrsBucket.getCdrFromKey(unpackedRatingResult.getUid());
            ///TODO not found management exception
            if(foundCdr!=null){
                if(unpackedRatingResult.getRatingResultsCount()>0){
                    for(ByteString ratingResult:unpackedRatingResult.getRatingResultsList()){
                        foundCdr.addRatingResultSerialized(ratingResult.toByteArray());
                    }
                }
                if(unpackedRatingResult.hasIsDuplicated()){
                    foundCdr.setDuplicated(unpackedRatingResult.getIsDuplicated());
                }
                if(unpackedRatingResult.hasIsDiscarded()){
                    foundCdr.setDiscarded(unpackedRatingResult.getIsDiscarded());
                }
                foundCdr.incOverheadCounter();
            }            
        }
        //cdrsBucket.setCheckSum(unpackedMessage.getEndingCheckSum());
    }
    
    
    /**
    *   pack a message for given list of Cdrs
    *   @param cdrsToStoreList The cdrMap from which to submit cdrs
     *   @return the array of bytes of the packed message (to be appended at the end of existing document)
    */
    private byte[] packStorageDocument(GenericCdrsBucket<T> cdrsToStoreList){
        if(cdrsToStoreList.getCdrBucketDocumentType().equals(GenericCdrsBucket.DocumentType.CDRS_BUCKET_FULL)){
            NormalCdrsAppender.Builder normalAppenderBuilder = NormalCdrsAppender.newBuilder();
             
            for(T cdrToStore:cdrsToStoreList.getCdrs()){
                NormalCdr.Builder cdrBuilder = NormalCdr.newBuilder();
                cdrBuilder.setUid(cdrToStore.getUid());
                cdrBuilder.setRawData(ByteString.copyFrom(cdrToStore.getCdrDataSerialized()));
                
                //Add rating result(s)
                for(Object ratingResultObj : cdrToStore.getRatingResultsSerialized()){
                    byte[] ratingResult = (byte[])ratingResultObj;
                    cdrBuilder.addRatingResults(ByteString.copyFrom(ratingResult));
                }
                if(cdrToStore.isDiscarded()){
                    cdrBuilder.setIsDiscarded(true);
                }
                if(cdrToStore.isDuplicated()){
                    cdrBuilder.setIsDuplicated(true);
                }
                normalAppenderBuilder.addNormalCdrs(cdrBuilder.build());
            }
            if(cdrsToStoreList.getDbDocSize()==null){
                cdrsToStoreList.setDbDocSize(0);
            }
            normalAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getDbDocSize());
            normalAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getDbDocSize() + normalAppenderBuilder.build().getSerializedSize());

            return normalAppenderBuilder.build().toByteArray();
        }
        else{
            PartialCdrsAppender.Builder partialAppenderBuilder = PartialCdrsAppender.newBuilder();
             
            for(T cdrToStore:cdrsToStoreList.getCdrs()){
                PartialCdrRecord.Builder cdrBuilder = PartialCdrRecord.newBuilder();
                cdrBuilder.setUid(cdrToStore.getUid());
                if(cdrToStore.getCdrData()!=null){
                    cdrBuilder.setRawData(ByteString.copyFrom(cdrToStore.getCdrDataSerialized()));
                }
                //Add rating result(s)
                for(Object ratingResultObj : cdrToStore.getRatingResultsSerialized()){
                    byte[] ratingResult = (byte[])ratingResultObj;
                    cdrBuilder.addRatingResults(ByteString.copyFrom(ratingResult));
                }
                if(cdrToStore.isDiscarded()){
                    cdrBuilder.setIsDiscarded(true);
                }
                if(cdrToStore.isDuplicated()){
                    cdrBuilder.setIsDuplicated(true);
                }
                partialAppenderBuilder.addPartialCdrs(cdrBuilder.build());
            }
            //Add Checksum
            if(cdrsToStoreList.getCdrBucketDocumentType().equals(GenericCdrsBucket.DocumentType.CDRS_BUCKET_PARTIAL_WITHOUT_CHECKSUM)){
                partialAppenderBuilder.setEndingCheckSum(0);
            }
            else{
                partialAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getDbDocSize());
                partialAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getDbDocSize() + partialAppenderBuilder.build().getSerializedSize());
            }
            return partialAppenderBuilder.build().toByteArray();
        }
        
    }
}
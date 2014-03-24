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
import com.dreameddeath.rating.storage.ActiveCdrsProtos.RatingResultCdrRecord;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.OverallCdrsMessage;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.NormalCdrsAppender;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.RatingResultAppender;

/**
*  Class used to perform storage preparation based on Raw Cdr Storage Managed (array of bytes)
*  Perform a checksum calculation (length) to allow rating request detection through couchbase request
*  The only contraint it that the Cdrs must have a unique id
*/
public abstract class RawCdrsMapTranscoder<T extends RawCdr> implements Transcoder<RawCdrsMap<T>>{
    abstract protected T rawCdrBuilder(String uid);
    
    
    @Override
    public int getMaxSize(){
        return CachedData.MAX_SIZE;
    }
    
    @Override
    public boolean asyncDecode(CachedData cachedData){
        return false;
    }
    
    
    @Override
    public RawCdrsMap<T> decode(CachedData cachedData){
        RawCdrsMap<T> result = new RawCdrsMap<T>(cachedData.getData().length, CouchbaseConstants.DocumentFlag.unPack(cachedData.getFlags()),false);
        try{
            unpackStorageDocument(result,cachedData.getData());
        }
        catch(InvalidProtocolBufferException e){
            ///TODO manage Error
            return null;
        }
        return result;
    }
    
    
    @Override
    public CachedData encode(RawCdrsMap<T> input){
        return new CachedData(CouchbaseConstants.DocumentFlag.pack(input.getFlags()),packStorageDocument(input),CachedData.MAX_SIZE);
    }
    


    /**
    *   Unpack a message containing Cdrs
    *   @param cdrsMap the resulting content
    *   @param message the message to unpack
    *   @throws InvalidProtocolBufferException when the message isn't well formatted
    *   @return a list of RawCdr for successfull found cdrs
    */
    private  void unpackStorageDocument(RawCdrsMap<T> cdrsMap, byte[] message) throws InvalidProtocolBufferException{
        //Unpack Cdrs
        OverallCdrsMessage unpackedMessage = OverallCdrsMessage.parseFrom(message);
        
        //Normal Cdrs uncompress and fill-up Hash Map
        for(NormalCdr unpackedCdr :unpackedMessage.getNormalCdrsList()){
            T foundCdr=rawCdrBuilder(unpackedCdr.getUid());
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
            cdrsMap.add(foundCdr);
        }
        
        //Additional Rating unpacking (rating and duplicate checks)
        for(RatingResultCdrRecord unpackedRatingResult:unpackedMessage.getRatingResultCdrsList()){
            T foundCdr = cdrsMap.get(unpackedRatingResult.getUid());
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
        cdrsMap.setCheckSum(unpackedMessage.getEndingCheckSum());
    }
    
    
    /**
    *   pack a message for given list of Cdrs
    *   @param cdrsToStoreList The cdrMap from which to submit cdrs
     *   @return the array of bytes of the packed message (to be appended at the end of existing document)
    */
    private byte[] packStorageDocument(RawCdrsMap<T> cdrsToStoreList){
        if(cdrsToStoreList.isIncrementalRating()){
            RatingResultAppender.Builder ratingAppenderBuilder = RatingResultAppender.newBuilder();
             
            for(T cdrToStore:cdrsToStoreList.values()){
                RatingResultCdrRecord.Builder cdrBuilder = RatingResultCdrRecord.newBuilder();
                cdrBuilder.setUid(cdrToStore.getUid());
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
                ratingAppenderBuilder.addRatingResultCdrs(cdrBuilder.build());
            }
            //Add dummy value to calculate size
            if(cdrsToStoreList.getCurrDbSize()<0){
                ratingAppenderBuilder.setEndingCheckSum(0);
            }
            else{
                ratingAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getCurrDbSize());
                ratingAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getCurrDbSize() + ratingAppenderBuilder.build().getSerializedSize());
            }
            return ratingAppenderBuilder.build().toByteArray();
        }
        else{
            NormalCdrsAppender.Builder normalAppenderBuilder = NormalCdrsAppender.newBuilder();
             
            for(T cdrToStore:cdrsToStoreList.values()){
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
            //Add dummy value to calculate size
            if(cdrsToStoreList.getCurrDbSize()<0){
                normalAppenderBuilder.setEndingCheckSum(0);
            }
            else{
                normalAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getCurrDbSize());
                normalAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getCurrDbSize() + normalAppenderBuilder.build().getSerializedSize());
            }
            return normalAppenderBuilder.build().toByteArray();
        }
    }
}
package com.dreameddeath.rating.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

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
public class RatingStorageManager{

    /**
    *   Unpack a message containing Cdrs
    *   @param message the array of bytes being the message containing cdrs
    *   @throws InvalidProtocolBufferException when the message isn't well formatted
    *   @return a list of RawCdr for successfull found cdrs
    */
    public static Collection<RawCdr> unpackStorageDocument(byte[] message) throws InvalidProtocolBufferException{
        Map<String,RawCdr> cdrsMap = new HashMap<String,RawCdr>();
        
        //Unpack Cdrs
        OverallCdrsMessage unpackedMessage = OverallCdrsMessage.parseFrom(message);
        
        //Normal Cdrs uncompress and fill-up Hash Map
        for(NormalCdr unpackedCdr :unpackedMessage.getNormalCdrsList()){
            RawCdr foundCdr=new RawCdr(unpackedCdr.getUid());
            foundCdr.setCdrData(unpackedCdr.getRawData().toByteArray());
            if(unpackedCdr.getRatingResultsCount()>0){
                for(ByteString ratingResult:unpackedCdr.getRatingResultsList()){
                    foundCdr.addRatingResult(ratingResult.toByteArray());
                }
            }
            if(unpackedCdr.hasIsDuplicated()){
                foundCdr.setDuplicated(unpackedCdr.getIsDuplicated());
            }
            if(unpackedCdr.hasIsDiscarded()){
                foundCdr.setDiscarded(unpackedCdr.getIsDiscarded());
            }
            
            ///TODO duplicate management exception
            cdrsMap.put(foundCdr.getUid(),foundCdr);
        }
        
        //Additional Rating unpacking (rating and duplicate checks)
        for(RatingResultCdrRecord unpackedRatingResult:unpackedMessage.getRatingResultCdrsList()){
            RawCdr foundCdr = cdrsMap.get(unpackedRatingResult.getUid());
            ///TODO not found management exception
            if(foundCdr!=null){
                if(unpackedRatingResult.getRatingResultsCount()>0){
                    for(ByteString ratingResult:unpackedRatingResult.getRatingResultsList()){
                        foundCdr.addRatingResult(ratingResult.toByteArray());
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
        System.out.println("Output message size "+unpackedMessage.getEndingCheckSum());
        return cdrsMap.values();
    }
    
    
    /**
    *   pack a message for given list of Cdrs
    *   @param cdrsList List of Cdrs to be added
    *   @param isPureRating Tell that the building should be made as a result of rating (without full Cdr)
    *   @param previousBucketSize give the previous cdr bucket size when dealing with cdrs to be rated
    *   @return the array of bytes of the packed message (to be appended at the end of existing request)
    */
    public static byte[] packStorageDocument(Collection<RawCdr> cdrsToStoreList,boolean isPureRating, int previousBucketSize){
        if(isPureRating){
            RatingResultAppender.Builder ratingAppenderBuilder = RatingResultAppender.newBuilder();
             
            for(RawCdr cdrToStore:cdrsToStoreList){
                RatingResultCdrRecord.Builder cdrBuilder = RatingResultCdrRecord.newBuilder();
                cdrBuilder.setUid(cdrToStore.getUid());
                //Add rating result(s)
                for(byte[] ratingResult:cdrToStore.getRatingResults()){
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
            if(previousBucketSize<0){
                ratingAppenderBuilder.setEndingCheckSum(0);
            }
            else{
                ratingAppenderBuilder.setEndingCheckSum(previousBucketSize);
                ratingAppenderBuilder.setEndingCheckSum(previousBucketSize + ratingAppenderBuilder.build().getSerializedSize());
            }
            return ratingAppenderBuilder.build().toByteArray();
        }
        else{
            NormalCdrsAppender.Builder normalAppenderBuilder = NormalCdrsAppender.newBuilder();
             
            for(RawCdr cdrToStore:cdrsToStoreList){
                NormalCdr.Builder cdrBuilder = NormalCdr.newBuilder();
                cdrBuilder.setUid(cdrToStore.getUid());
                cdrBuilder.setRawData(ByteString.copyFrom(cdrToStore.getCdrData()));
                
                //Add rating result(s)
                for(byte[] ratingResult:cdrToStore.getRatingResults()){
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
            if(previousBucketSize<0){
                normalAppenderBuilder.setEndingCheckSum(0);
            }
            else{
                normalAppenderBuilder.setEndingCheckSum(previousBucketSize);
                normalAppenderBuilder.setEndingCheckSum(previousBucketSize + normalAppenderBuilder.build().getSerializedSize());
            }
            return normalAppenderBuilder.build().toByteArray();
        }
    }

}
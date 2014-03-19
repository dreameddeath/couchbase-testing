package com.dreameddeath.rating.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import com.dreameddeath.rating.storage.ActiveCdrsProtos.NormalCdr; 
import com.dreameddeath.rating.storage.ActiveCdrsProtos.RatingResultCdrRecord;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.OverallCdrsMessage;

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
                    foundCdr.addCdrRatingResults(ratingResult.toByteArray());
                }
            }
            if(unpackedCdr.hasIsDuplicate()){
                foundCdr.setDuplicate(unpackedCdr.getIsDuplicate());
            }
            ///TODO duplicate management exception
            cdrsMap.put(foundCdr.getUid(),foundCdr);
        }
        
        //Additional Rating unpacking (rating and duplicate checks)
        for(RatingResultCdrRecord unpackedRatingResult:unpackedMessage.getRatingResultCdrsList()){
            RawCdr foundCdr = cdrsMap.get(unpackedRatingResult.getUid());
            ///TODO not found management exception
            if(foundCdr!=null){
                foundCdr.addCdrRatingResults(unpackedRatingResult.getRatingResult().toByteArray());
                foundCdr.incOverheadCounter();
            }
            if(unpackedRatingResult.hasIsDuplicate()){
                foundCdr.setDuplicate(unpackedRatingResult.getIsDuplicate());
            }
        }
        
        return cdrsMap.values();
    }


    /**
    *   pack a message for given list of Cdrs
    *   @param cdrsList List of Cdrs to be added
    *   @param triggerRating tell to put a marker to trigger the rating
    *   @return the array of bytes of the packed message (to be appended at the end of existing request)
    */
    public static byte[] unpackStorageDocument(Collection<RawCdr> cdrsList,boolean triggerRating){
        return new byte[1];
    }

}
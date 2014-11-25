package com.dreameddeath.rating.storage;


import com.couchbase.client.core.lang.Tuple;
import com.couchbase.client.core.lang.Tuple2;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.dreameddeath.core.model.binary.BinaryCouchbaseDocument;
import com.dreameddeath.core.model.document.BucketDocument;
import com.dreameddeath.core.storage.GenericTranscoder;
import com.dreameddeath.rating.model.cdr.GenericCdr;
import com.dreameddeath.rating.model.cdr.GenericCdrsBucket;
import com.dreameddeath.rating.storage.ActiveCdrsProtos.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;


/**
*  This abstract class is use to manage binary transcoding of a bucket of CDR. It exists with three modes :
*  - CDRS_BUCKET_FULL : it is a full storage
*  - CDRS_BUCKET_PARTIAL_WITHOUT_CHECKSUM : the storage is in append mode but without checksum to trigger rating when needed
*  - CDRS_BUCKET_PARTIAL_WITH_CHECKSUM : add the checksum to try to give a proper checksum to avoid rerating
*
*  The cdr itself is managed by the @see GenericCdr class.
*/
public abstract class GenericCdrsBucketTranscoder<T extends GenericCdr,TBUCKET extends GenericCdrsBucket<T>> extends GenericTranscoder<TBUCKET> {
    /**
    * abstract "callback" function used to initialize the CDR while unpacking
    */
    abstract protected T genericCdrBuilder(String uid);
    
    /**
    * abstract "callback" function used to initialize the CDR Bucket while unpacking
    */
    abstract protected TBUCKET genericCdrBucketBuilder(BinaryCouchbaseDocument.BinaryDocumentType docType);

    public GenericCdrsBucketTranscoder(Class<TBUCKET> clazz, Class<? extends BucketDocument<TBUCKET>> baseDocumentClazz){ super(clazz,baseDocumentClazz);}
    /**
    * Decode the database CDR bucket
    * @param id the bucket key
    * @param content the binary content
    * @param cas the current database cas
    * @param expiry the current expiration
    * @param flags the flags
    * @param status the response status
    * */
    @Override
    public BucketDocument<TBUCKET> decode(String id, ByteBuf content, long cas, int expiry, int flags, ResponseStatus status) {
        try {
            TBUCKET result = genericCdrBucketBuilder(BinaryCouchbaseDocument.BinaryDocumentType.BINARY_FULL);
            result.getBaseMeta().setDbSize(content.array().length);
            unpackStorageDocument(result,content.array());
            return newDocument(id,expiry,result,cas);
        }
        catch(InvalidProtocolBufferException e){
            return null;
        }
        //return result;
    }

    /**
     * Encode The CDR bucket prior to database storage (can be in append mode)
     * @param document the CDR bucket to be encoded
     */
    @Override
    public Tuple2<ByteBuf, Integer> encode(BucketDocument<TBUCKET> document) {
        byte[] encoded = packStorageDocument(document.content());
        document.content().getBinaryMeta().setLastWrittenSize(encoded.length);
        return Tuple.create(Unpooled.wrappedBuffer(encoded), document.content().getBaseMeta().getEncodedFlags());
    }



    /**
    * Unpack a message containing Cdrs
    * @param cdrsBucket the cdrs will be added to them
    * @param message the message to unpack (parse) and add them to the cdrsBucket parameter
    * @throws InvalidProtocolBufferException when the message isn't well formatted
    */
    private  void unpackStorageDocument(TBUCKET cdrsBucket, byte[] message) throws InvalidProtocolBufferException{
        //Unpack Cdrs
        OverallCdrsMessage unpackedMessage = OverallCdrsMessage.parseFrom(message);
        
        //Read Link keys
        cdrsBucket.setBillingAccountKey(unpackedMessage.getBaKey());
        cdrsBucket.setRatingContextKey(unpackedMessage.getRatingCtxtKey());
        cdrsBucket.setBillingCycleKey(unpackedMessage.getBaCycleKey());
        
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
        cdrsBucket.getBinaryMeta().setEndingCheckSum(unpackedMessage.getEndingCheckSum());
    }
    
    
    /**
    * pack a message for given list of Cdrs
    * @param cdrsToStoreList The cdrMap from which to submit cdrs
    * @return the array of bytes of the packed message (to be appended at the end of existing document)
    */
    private byte[] packStorageDocument(TBUCKET cdrsToStoreList){
        //If it is a full bucket, use the normal cdrs writer
        if(cdrsToStoreList.getBinaryMeta().getBinaryDocumentType().equals(BinaryCouchbaseDocument.BinaryDocumentType.BINARY_FULL)){
            NormalCdrsAppender.Builder normalAppenderBuilder = NormalCdrsAppender.newBuilder();
            
            normalAppenderBuilder.setBaKey(cdrsToStoreList.getBillingAccountKey());
            normalAppenderBuilder.setRatingCtxtKey(cdrsToStoreList.getRatingContextKey());
            normalAppenderBuilder.setBaCycleKey(cdrsToStoreList.getBillingCycleKey());
            
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
            if(cdrsToStoreList.getBaseMeta().getDbSize()==null){
                cdrsToStoreList.getBaseMeta().setDbSize(0);
            }
            normalAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getBaseMeta().getDbSize());
            normalAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getBaseMeta().getDbSize() + normalAppenderBuilder.build().getSerializedSize());

            return normalAppenderBuilder.build().toByteArray();
        }
        //If it is a partial bucket, use a partial cdrs bucket
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
            
            //if it is a partial cdr to add, build a fake checksum
            if(cdrsToStoreList.getBinaryMeta().getBinaryDocumentType().equals(BinaryCouchbaseDocument.BinaryDocumentType.BINARY_PARTIAL_WITHOUT_CHECKSUM)){
                partialAppenderBuilder.setEndingCheckSum(0);
            }
            //else it is a nominal process, try to build a valid checksum
            else{
                partialAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getBaseMeta().getDbSize());
                partialAppenderBuilder.setEndingCheckSum(cdrsToStoreList.getBaseMeta().getDbSize() + partialAppenderBuilder.build().getSerializedSize());
            }
            return partialAppenderBuilder.build().toByteArray();
        }
        
    }
}
package com.dreamddeath.couchbase_testing;

import com.couchbase.client.CouchbaseClient;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
/*import com.dreameddeath.couchbase_testing.RatingContextProtos.RatingContext; 
import com.dreameddeath.couchbase_testing.RatingContextProtos.RatingBucket;
import com.dreameddeath.couchbase_testing.RatingContextProtos.Cdrs;
import com.dreameddeath.couchbase_testing.RatingContextProtos.RatingContextAppender;
import com.google.protobuf.CodedOutputStream;
*/

import com.dreameddeath.rating.storage.RawCdr;
import com.dreameddeath.rating.storage.RatingStorageManager;

public class CouchbaseConnection {
     /*class DecodedStructElem {
        public int tag;
        public int startPos;
        public int endPos;
     }
     byte[] encodeToBytes(byte[] src,int tag){
        byte[] outputBytes=new byte[CodedOutputStream.computeInt32Size(1,src.length)+src.length];
        CodedOutputStream encoder = CodedOutputStream.newInstance(outputBytes);
        encoder.writeInt32(1,src.length);
        encoder.writeRawBytes(src);
        return outputBytes;
     }

     List<DecodedStructElem> decodeToBytes(byte[] src){
        CodedInputStream
        byte[] outputBytes=new byte[CodedOutputStream.computeInt32Size(1,src.length)+src.length];
        CodedOutputStream encoder = CodedOutputStream.newInstance(outputBytes);
        encoder.writeInt32(1,src.length);
        encoder.writeRawBytes(src);
        return outputBytes;
     }*/

     public static void main(String[] args) throws Exception {
        // (Subset) of nodes in the cluster to establish a connection
        List<URI> hosts = Arrays.asList(
          new URI("http://127.0.0.1:8091/pools")
        );
     
        // Name of the Bucket to connect to
        String bucket = "test";
     
        // Password of the bucket (empty) string if none
        String password = "adminuser";
     
        // Connect to the Cluster
        CouchbaseClient client = new CouchbaseClient(hosts, bucket, password);
     
  /*      RatingContext.Builder ratingContextBuilder = RatingContext.newBuilder();
        ratingContextBuilder.setName("Christophe Jeunesse");
        ratingContextBuilder.setId(1234);
            
        for(int i=0;i<3;i++){
            RatingBucket.Builder ratingBucketBuilder = RatingBucket.newBuilder();
            ratingBucketBuilder.setName("Bucket "+i);
            ratingBucketBuilder.setStartAmount((i+1)*1000);
            ratingBucketBuilder.setRemainingAmount((i+1)*1000-100*i);
            ratingContextBuilder.addBuckets(ratingBucketBuilder.build());
        }
        ratingContextBuilder.setFinishing("DONE");*/
        List<RawCdr> list=new ArrayList<RawCdr>();
        for(int i=0;i<5;++i){
            RawCdr cdr = new RawCdr("CDR_"+i);
            cdr.setCdrData(("BaseCdrContent_"+i).getBytes());
            list.add(cdr);
        }
        
        // Store a Document
        client.set("my-first-document2",RatingStorageManager.packStorageDocument(list,false,-1)).get();
        byte[] rawMessage = ((byte[])client.get("my-first-document2"));
        int storageSize = rawMessage.length;
        System.out.println("Real Message size "+storageSize);
        Collection<RawCdr> unpackedList = RatingStorageManager.unpackStorageDocument(rawMessage);
        System.out.println("Result "+unpackedList.toString());
        
        for(RawCdr cdr : list){
            cdr.setCdrData(null);
            cdr.addRatingResult(("RatingContext_"+list.indexOf(cdr)).getBytes());
            cdr.addRatingResult(("RatingContext2_"+list.indexOf(cdr)).getBytes());
        }
        list.remove(0);
        list.remove(0);
        client.append("my-first-document2",RatingStorageManager.packStorageDocument(list,true,storageSize)).get();
        rawMessage = ((byte[])client.get("my-first-document2"));
        storageSize = rawMessage.length;
        System.out.println("Real Message size "+storageSize);
        unpackedList = RatingStorageManager.unpackStorageDocument(rawMessage);
        System.out.println("Result "+unpackedList.toString());
        /*RatingContextAppender.Builder appender = RatingContextAppender.newBuilder();
        for(int i=3;i<6;i++){
            RatingBucket.Builder ratingBucketBuilder = RatingBucket.newBuilder();
            ratingBucketBuilder.setName("Bucket "+i);
            ratingBucketBuilder.setStartAmount((i+1)*1000);
            ratingBucketBuilder.setRemainingAmount((i+1)*1000-100*i);
            appender.addBuckets(ratingBucketBuilder.build());
        }
        appender.setFinishing("ERRO");*/
        // Store a Document
        //client.append("my-first-document2",appender.build().toByteArray()).get();
        
        //byte[] message=(byte[])client.get("my-first-document2");
        //RatingContext ctxt =RatingContext.parseFrom(message);
        // Retreive the Document and print it
        //System.out.println(ctxt.getName()+ " "+ctxt.getBucketsCount()+ " "+ctxt.getFinishing());
     
        // Shutting down properly
        client.shutdown();
  }

}
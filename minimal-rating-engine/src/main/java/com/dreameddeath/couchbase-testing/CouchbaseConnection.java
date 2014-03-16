package com.dreamddeath.couchbase_testing;

import com.couchbase.client.CouchbaseClient;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import com.dreameddeath.couchbase_testing.RatingContextProtos.RatingContext; 
import com.dreameddeath.couchbase_testing.RatingContextProtos.RatingBucket;
import com.dreameddeath.couchbase_testing.RatingContextProtos.Cdrs;
import com.dreameddeath.couchbase_testing.RatingContextProtos.RatingContextAppender;
import com.google.protobuf.CodedOutputStream;


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
     
        RatingContext.Builder ratingContextBuilder = RatingContext.newBuilder();
        ratingContextBuilder.setName("Christophe Jeunesse");
        ratingContextBuilder.setId(1234);
            
        for(int i=0;i<3;i++){
            RatingBucket.Builder ratingBucketBuilder = RatingBucket.newBuilder();
            ratingBucketBuilder.setName("Bucket "+i);
            ratingBucketBuilder.setStartAmount((i+1)*1000);
            ratingBucketBuilder.setRemainingAmount((i+1)*1000-100*i);
            ratingContextBuilder.addBuckets(ratingBucketBuilder.build());
        }
        ratingContextBuilder.setFinishing("DONE");
        // Store a Document
        client.set("my-first-document",ratingContextBuilder.build().toByteArray()).get();
        
        RatingContextAppender.Builder appender = RatingContextAppender.newBuilder();
        for(int i=3;i<6;i++){
            RatingBucket.Builder ratingBucketBuilder = RatingBucket.newBuilder();
            ratingBucketBuilder.setName("Bucket "+i);
            ratingBucketBuilder.setStartAmount((i+1)*1000);
            ratingBucketBuilder.setRemainingAmount((i+1)*1000-100*i);
            appender.addBuckets(ratingBucketBuilder.build());
        }
        appender.setFinishing("ERRO");
        // Store a Document
        client.append("my-first-document",appender.build().toByteArray()).get();
        
        byte[] message=(byte[])client.get("my-first-document");
        RatingContext ctxt =RatingContext.parseFrom(message);
        // Retreive the Document and print it
        System.out.println(ctxt.getName()+ " "+ctxt.getBucketsCount()+ " "+ctxt.getFinishing());
     
        // Shutting down properly
        client.shutdown();
  }

}
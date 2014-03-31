package com.dreamddeath.couchbase_testing;

import com.couchbase.client.CouchbaseClient;
import java.net.URI;
import java.io.Externalizable;
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

import net.spy.memcached.transcoders.Transcoder;
import com.dreameddeath.common.storage.BinarySerializer;
import com.dreameddeath.rating.storage.*;
import com.dreameddeath.rating.model.context.*;

public class CouchbaseConnection {
     public static class StringSerializer implements BinarySerializer<String>{
        public byte[] serialize(String str){
            return str.getBytes();
        }
        public String deserialize(byte[] input){
            return new String(input);
        }
     }
     
     public static class StringCdr extends RawCdr<String,String>{
        private static StringSerializer _serializer = new StringSerializer();
        
        public StringCdr(String uid){
            super(uid);
        }
        
        @Override
        protected BinarySerializer<String> getCdrDataSerializer(){
            return _serializer;
        }
        protected BinarySerializer<String> getCdrRatingSerializer(){
            return _serializer;
        }
   
     }
     public static class StringCdrRatingTrancoder extends RawCdrsMapTranscoder<StringCdr>{
        @Override
        protected StringCdr rawCdrBuilder(String uid){
            return new StringCdr(uid);
        }
     }
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
        
        StandardRatingContext ratingCtxt = new StandardRatingContext();
        ratingCtxt.setUid("ratCxt/1");
        
        client.set(ratingCtxt.getUid(),0,ratingCtxt,ratingCtxt.getTranscoder());
        
        StringCdrRatingTrancoder transcoder = new StringCdrRatingTrancoder();
        //List<RawCdr> list=new ArrayList<RawCdr>();
        RawCdrsMap<StringCdr> cdrsMap = new RawCdrsMap<StringCdr>();
        for(int i=0;i<5;++i){
            StringCdr cdr = new StringCdr("CDR_"+i);
            cdr.setCdrData("BaseCdrContent_"+i);
            cdrsMap.add(cdr);
        }
        
        // Store a Document
        client.set("my-first-document2",0,cdrsMap,transcoder).get();
        RawCdrsMap<StringCdr> unpackedCdrsMap = client.get("my-first-document2", transcoder);
        System.out.println("Result :\n"+unpackedCdrsMap.toString());
        
        RawCdrsMap<StringCdr> newCdrsMap = new RawCdrsMap<StringCdr>(unpackedCdrsMap.getCurrDbSize(),true);
        int pos=0;
        for(StringCdr cdr : unpackedCdrsMap.values()){
            if(pos%2==0){
                StringCdr updatedCdr = new StringCdr(cdr.getUid());
                updatedCdr.addRatingResult("RatingContext_"+cdr.getUid());
                updatedCdr.addRatingResult("RatingContext2_"+cdr.getUid());
                newCdrsMap.add(updatedCdr);
            }
            pos++;
        }
        
        client.append("my-first-document2",newCdrsMap,transcoder).get();
        unpackedCdrsMap = client.get("my-first-document2", transcoder);
        System.out.println("Result :\n"+unpackedCdrsMap.toString());
        
        
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
        /*List<RawCdr> list=new ArrayList<RawCdr>();
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
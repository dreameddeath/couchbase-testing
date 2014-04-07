package com.dreamddeath.couchbase_testing;

import com.couchbase.client.CouchbaseClient;
import java.net.URI;
import java.io.Externalizable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import net.spy.memcached.transcoders.Transcoder;

import java.util.concurrent.Future;
import net.spy.memcached.internal.OperationFuture;

import com.dreameddeath.common.storage.BinarySerializer;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;
import com.dreameddeath.common.storage.OperationFutureWrapper;
import com.dreameddeath.common.dao.*;
import com.dreameddeath.rating.storage.*;
import com.dreameddeath.rating.model.context.*;
import com.dreameddeath.billing.model.*;
import com.dreameddeath.billing.dao.*;
import com.dreameddeath.rating.dao.context.*;

public class CouchbaseConnection {
    protected static final CouchbaseClientWrapper _client;
    static{
        CouchbaseClient realClient=null;
        try{
            // (Subset) of nodes in the cluster to establish a connection
            List<URI> hosts = Arrays.asList(new URI("http://127.0.0.1:8091/pools"));
        
            // Name of the Bucket to connect to
            String bucket = "test";
            // Password of the bucket (empty) string if none
            String password = "adminuser";
            // Connect to the Cluster
            realClient = new CouchbaseClient(hosts, bucket, password);
        }
        catch(Exception e){
            
        }
        
        _client = new CouchbaseClientWrapper(realClient);
    }

    private static final CouchbaseDocumentDaoFactory _daoFactory = new CouchbaseDocumentDaoFactory();
    static {
        _daoFactory.addDaoFor(BillingAccount.class,new BillingAccountDao(_client,_daoFactory));
        _daoFactory.addDaoFor(BillingCycle.class,new BillingCycleDao(_client,_daoFactory));
        _daoFactory.addDaoFor(AbstractRatingContext.class,new AbstractRatingContextDao(_client,_daoFactory));
        _daoFactory.addDaoFor(StringCdrBucket.class,new StringCdrBucketDao(_client,_daoFactory));
    }
    
    public static class StringSerializer implements BinarySerializer<String>{
        public byte[] serialize(String str){ return str.getBytes(); }
        public String deserialize(byte[] input){ return new String(input); }
    }

    public static class StringCdr extends GenericCdr<String,String>{
        private static StringSerializer _serializer = new StringSerializer();

        public StringCdr(String uid){ super(uid); }
        protected BinarySerializer<String> getCdrDataSerializer(){ return _serializer; }
        protected BinarySerializer<String> getCdrRatingSerializer(){ return _serializer; }
    }

    public static class StringCdrBucket extends GenericCdrsBucket<StringCdr>{
        public StringCdrBucket(GenericCdrsBucket.DocumentType docType){ super(docType); }
        public StringCdrBucket(String key,Integer origDbSize,DocumentType documentType){ super(key,origDbSize,documentType); }
    }
    
    // public static CdrsBucketLink<GenericCdrsBucket> buildLink(T genCdrsBucket){
        // CdrsBucketLink<T> newLink = new CdrsBucketLink<T>();
        // newLink.setKey(genCdrsBucket.getKey());
        // newLink.setType(genCdrsBucket.getClass().getSimpleName());
        // newLink.updateFromBucket(genCdrsBucket);
        // newLink.setLinkedObject(genCdrsBucket);
        // return newLink;
    // }
    public static class StringCdrRatingTrancoder extends GenericCdrsBucketTranscoder<StringCdr,StringCdrBucket>{
        @Override
        protected StringCdr genericCdrBuilder(String uid){ return new StringCdr(uid); }

        @Override
        protected StringCdrBucket genericCdrBucketBuilder(GenericCdrsBucket.DocumentType docType){ return new StringCdrBucket(docType); }
    }
    
    
    
    public static class StringCdrBucketDao extends CouchbaseDocumentDao<StringCdrBucket>{
        public static final String CDR_BUCKET_CNT_KEY="%s/cdrs/cnt";
        public static final String CDR_BUCKET_FMT_KEY="%s/cdrs/%d";
    
        private static StringCdrRatingTrancoder _tc = new StringCdrRatingTrancoder();
    
        public  Transcoder<StringCdrBucket> getTranscoder(){
            return _tc;
        }
       
        public StringCdrBucketDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
            super(client,factory);
        }
    
        public void buildKey(StringCdrBucket obj){
            long result = getClientWrapper().getClient().incr(String.format(CDR_BUCKET_CNT_KEY,obj.getRatingContextKey()),1,1,0);
            obj.setKey(String.format(CDR_BUCKET_FMT_KEY,obj.getRatingContextKey(),result));
        }
    }
    
    public static void main(String[] args) throws Exception {
        _client.getClient().flush();
        try{
            BillingAccount ba = new BillingAccount();
            //ba.setKey("ba/1");
            //ba.setUid("1");
            //OperationFutureWrapper<Boolean,BillingAccount> future=client.set(ba);
            //future.get();
            //System.out.println("Set Cas :"+future.getFuture().getCas());
            BillingCycle billCycle = new BillingCycle();
            //billCycle.setKey(ba.getKey()+"/c/1");
            billCycle.setBillingAccountLink(new BillingAccountLink(ba));
            billCycle.setStartDate((new DateTime()).withTime(0,0,0,0));
            billCycle.setEndDate(billCycle.getStartDate().plusMonths(1));
            
            StandardRatingContext ratingCtxt = new StandardRatingContext();
            //ratingCtxt.setUid("ratCxt/1");
            ratingCtxt.setBillingAccountLink(new BillingAccountLink(ba));
            ratingCtxt.setBillingCycleLink(new BillingCycleLink(billCycle));
            billCycle.addRatingContextLink(new RatingContextLink(ratingCtxt));
            ba.addBillingCycle(new BillingCycleLink(billCycle));
            _daoFactory.getDaoFor(BillingAccount.class).create(ba);
            _daoFactory.getDaoFor(BillingCycle.class).create(billCycle);
            _daoFactory.getDaoFor(AbstractRatingContext.class).create(ratingCtxt);
            //_daoFactory.getDaoFor(BillingCycle.class).update(billCycle);
            //_daoFactory.getDaoFor(AbstractRatingContext.class).update(ratingCtxt);
            //_daoFactory.getDaoFor(BillingAccount.class).update(ba);
            
            System.out.println("Set Ba Result :"+ba);
            
            System.out.println("Set Cycle Result :"+billCycle);
            System.out.println("Set Rating ctxt Result :"+ratingCtxt);
            
            
            StringCdrBucket cdrsBucket = new StringCdrBucket(GenericCdrsBucket.DocumentType.CDRS_BUCKET_FULL);
            cdrsBucket.setBillingAccountKey(ba.getKey());
            cdrsBucket.setBillingCycleKey(billCycle.getKey());
            cdrsBucket.setRatingContextKey(ratingCtxt.getKey());
            //cdrsBucket.setKey("my-first-document2");
            for(int i=0;i<5;++i){
                StringCdr cdr = new StringCdr("CDR_"+i);
                cdr.setCdrData("BaseCdrContent_"+i);
                cdrsBucket.addCdr(cdr);
            }
            
            _daoFactory.getDaoFor(StringCdrBucket.class).create(cdrsBucket);
            
            
            GenericCdrsBucket<StringCdr> unpackedCdrsMap = _client.gets(cdrsBucket.getKey(),_daoFactory.getDaoFor(StringCdrBucket.class).getTranscoder());
            System.out.println("Result :\n"+unpackedCdrsMap.toString());
            
            StringCdrBucket newCdrsBucket = new StringCdrBucket(unpackedCdrsMap.getKey(),unpackedCdrsMap.getDbDocSize(),GenericCdrsBucket.DocumentType.CDRS_BUCKET_PARTIAL_WITH_CHECKSUM);
            int pos=0;
            for(StringCdr cdr : unpackedCdrsMap.getCdrs()){
                if(pos%2==0){
                    StringCdr updatedCdr = new StringCdr(cdr.getUid());
                    updatedCdr.addRatingResult("RatingContext_"+cdr.getUid());
                    updatedCdr.addRatingResult("RatingContext2_"+cdr.getUid());
                    newCdrsBucket.addCdr(updatedCdr);
                }
                pos++;
            }
            
            _client.append(newCdrsBucket,_daoFactory.getDaoFor(StringCdrBucket.class).getTranscoder()).get();
            unpackedCdrsMap = _client.gets(cdrsBucket.getKey(), _daoFactory.getDaoFor(StringCdrBucket.class).getTranscoder());
            System.out.println("Result :\n"+unpackedCdrsMap.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        _client.shutdown();
  }

}
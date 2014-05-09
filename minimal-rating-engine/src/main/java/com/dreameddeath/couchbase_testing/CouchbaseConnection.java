package com.dreameddeath.couchbase_testing;

import com.couchbase.client.CouchbaseClient;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import net.spy.memcached.transcoders.Transcoder;

import com.dreameddeath.common.storage.BinarySerializer;
import com.dreameddeath.common.storage.CouchbaseClientWrapper;
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
        public static final String CDR_BUCKET_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cdrs/\\d+";
    
        private static StringCdrRatingTrancoder _tc = new StringCdrRatingTrancoder();
    
        public  Transcoder<StringCdrBucket> getTranscoder(){
            return _tc;
        }
       
        public StringCdrBucketDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
            super(client,factory);
        }
    
        public void buildKey(StringCdrBucket obj){
            long result = getClientWrapper().getClient().incr(String.format(CDR_BUCKET_CNT_KEY,obj.getBillingAccountKey()),1,1,0);
            obj.setKey(String.format(CDR_BUCKET_FMT_KEY,obj.getBillingAccountKey(),result));
        }
        public String getKeyPattern(){
            return CDR_BUCKET_KEY_PATTERN;
        }
    }
    
    public static void main(String[] args) throws Exception {
        //_client.getClient().flush().get();
        try{
            CouchbaseSession session=_daoFactory.newSession();
            
            BillingAccount ba = session.newEntity(BillingAccount.class);
            ba.setLedgerSegment("test");
            BillingCycle billCycle =  session.newEntity(BillingCycle.class);
            billCycle.setBillingAccount(ba);
            billCycle.setStartDate((new DateTime()).withTime(0,0,0,0));
            billCycle.setEndDate(billCycle.getStartDate().plusMonths(1));
            
            StandardRatingContext ratingCtxt = session.newEntity(StandardRatingContext.class);
            ratingCtxt.setBillingCycle(billCycle);
            RatingContextAttribute attr =  new RatingContextAttribute();
            ratingCtxt.addAttribute(attr);
            attr.setCode("testing");
            //billCycle.addRatingContext(ratingContext.newRatingContextLink(ratingCtxt));
            //ba.addBillingCycle(new BillingCycleLink(billCycle));
            System.out.println("PreCreate Ba Result :"+ba);
            session.create(ba);
            session.create(billCycle);
            session.create(ratingCtxt);
            
            System.out.println("Set Rating Result :"+ratingCtxt);
            //BillingAccount readBa = _daoFactory.getDaoForClass(BillingAccount.class).get(ba.getKey());
            //System.out.println("Read Ba Result :"+readBa);
            //readBa.setLedgerSegment("Bis");
            attr.setCode("testing2");
            System.out.println("After Update Rating Result :"+ratingCtxt);
            
            StringCdrBucket cdrsBucket = new StringCdrBucket(GenericCdrsBucket.DocumentType.CDRS_BUCKET_FULL);
            cdrsBucket.setBillingAccountKey(ba.getKey());
            cdrsBucket.setBillingCycleKey(billCycle.getKey());
            cdrsBucket.setRatingContextKey(ratingCtxt.getKey());
            
            for(int i=0;i<5;++i){
                StringCdr cdr = new StringCdr("CDR_"+i);
                cdr.setCdrData("BaseCdrContent_"+i);
                cdrsBucket.addCdr(cdr);
            }
            
            _daoFactory.getDaoForClass(StringCdrBucket.class).create(cdrsBucket);
            
            GenericCdrsBucket<StringCdr> unpackedCdrsMap = _client.gets(cdrsBucket.getKey(),_daoFactory.getDaoForClass(StringCdrBucket.class).getTranscoder());
            
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
            
            _client.append(newCdrsBucket,_daoFactory.getDaoForClass(StringCdrBucket.class).getTranscoder()).get();
            unpackedCdrsMap = _client.gets(cdrsBucket.getKey(), _daoFactory.getDaoForClass(StringCdrBucket.class).getTranscoder());
            //System.out.println("Result :\n"+unpackedCdrsMap.toString());
            
            System.out.println("New Session");
            
            CouchbaseSession readSession=_daoFactory.newSession();
            BillingAccount readBa = readSession.get(ba.getKey(),BillingAccount.class);
            System.out.println("Ba Read finished");
            BillingCycle readCycle = readSession.get(billCycle.getKey(),BillingCycle.class);
            System.out.println("Cycle Read finished");
            System.out.println("Read Ba Result :"+readBa);
            System.out.println("Read BillCycle Result :"+readCycle);
            System.out.println("Read Cycle link :<"+readBa.getBillingCycleLinks().get(0).getLinkedObject(true)+">");
            
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
        _client.shutdown();
  }

}
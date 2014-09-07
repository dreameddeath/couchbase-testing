package com.dreameddeath.couchbase_testing;

import com.couchbase.client.CouchbaseClient;
import com.dreameddeath.billing.dao.BillingAccountDao;
import com.dreameddeath.billing.dao.BillingCycleDao;
import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.billing.process.CreateBillingAccountJob;
import com.dreameddeath.core.dao.CouchbaseSessionFactory;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.CouchbaseSession;
import com.dreameddeath.core.dao.document.JobDao;
import com.dreameddeath.core.dao.validation.ValidatorFactory;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.process.ProcessingServiceFactory;
import com.dreameddeath.core.storage.BinarySerializer;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.party.dao.PartyDao;
import com.dreameddeath.party.model.base.Party;
import com.dreameddeath.party.model.process.CreatePartyRequest;
import com.dreameddeath.party.process.CreatePartyJob;
import com.dreameddeath.rating.dao.RatingContextDao;
import com.dreameddeath.rating.model.context.RatingContext;
import com.dreameddeath.rating.model.cdr.GenericCdr;
import com.dreameddeath.rating.model.cdr.GenericCdrsBucket;
import com.dreameddeath.rating.storage.GenericCdrsBucketTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CouchbaseConnection {
    protected static final CouchbaseClientWrapper _client;
    static{
        CouchbaseClient realClient=null;
        try{
            // (Subset) of nodes in the cluster to establish a connection
            /*List<URI> hosts = Arrays.asList(new URI("http://192.168.1.5:8091/pools"));*/
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

    private static final CouchbaseCounterDaoFactory _counterDaoFactory = new CouchbaseCounterDaoFactory();

    private static final CouchbaseDocumentDaoFactory _docDaoFactory = new CouchbaseDocumentDaoFactory();
    static {
        _docDaoFactory.setCounterDaoFactory(_counterDaoFactory);
        _docDaoFactory.setValidatorFactory(new ValidatorFactory());
        _docDaoFactory.addDaoFor(BillingAccount.class, new BillingAccountDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(BillingCycle.class, new BillingCycleDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(RatingContext.class, new RatingContextDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(StringCdrBucket.class, new StringCdrBucketDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(Party.class, new PartyDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(AbstractJob.class, new JobDao(_client, _docDaoFactory));
    }

    private static final CouchbaseSessionFactory _sessionFactory = new CouchbaseSessionFactory(_docDaoFactory,_counterDaoFactory);
    
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
        public static final String CDR_BUCKET_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cdrs/cnt";

        private static StringCdrRatingTrancoder _tc = new StringCdrRatingTrancoder();
    
        public  Transcoder<StringCdrBucket> getTranscoder(){
            return _tc;
        }
       
        public StringCdrBucketDao(CouchbaseClientWrapper client,CouchbaseDocumentDaoFactory factory){
            super(client,factory);
            registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(CDR_BUCKET_CNT_PATTERN).withDefaultValue(1L));
        }

        public void buildKey(StringCdrBucket obj) throws DaoException{
            long result = obj.getSession().incrCounter(String.format(CDR_BUCKET_CNT_KEY, obj.getBillingAccountKey()), 1);
            obj.setKey(String.format(CDR_BUCKET_FMT_KEY,obj.getBillingAccountKey(),result));
        }

        public String getKeyPattern(){
            return CDR_BUCKET_KEY_PATTERN;
        }
    }
    
    public static void main(String[] args) throws Exception {
        //_client.getClient().flush().get();
        try{
            bench();
            //bench();
            /*CouchbaseSession session=_docDaoFactory.newSession();
            ProcessingServiceFactory serviceFactory = new ProcessingServiceFactory();

            CreatePartyJob createPartyJob = session.newEntity(CreatePartyJob.class);
            createPartyJob.request = new CreatePartyRequest();
            createPartyJob.request.type = CreatePartyRequest.Type.person;
            createPartyJob.request.person = new CreatePartyRequest.Person();
            createPartyJob.request.person.firstName = "christophe";
            createPartyJob.request.person.lastName = "jeunesse";
            serviceFactory.getJobServiceForClass(CreatePartyJob.class).execute(createPartyJob);

            session.clean();

            CreatePartyJob readJob = (CreatePartyJob)session.get(createPartyJob.getKey());
            System.out.println("Job <"+readJob.getKey()+"> status <"+readJob.getDocState()+">");
            System.out.println("PartyUID <" + ((CreatePartyJob.CreatePartyTask) readJob.getTasks().get(0)).getDocument().getUid() + ">");

            CreateBillingAccountJob createBaJob = session.newEntity(CreateBillingAccountJob.class);
            createBaJob.billDay = 2;
            createBaJob.partyId = ((CreatePartyJob.CreatePartyTask) readJob.getTasks().get(0)).getDocument().getUid();
            serviceFactory.getJobServiceForClass(CreateBillingAccountJob.class).execute(createBaJob);
            */
            /*BillingAccount ba = session.newEntity(BillingAccount.class);
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
            System.out.println("PreCreate Ba Result :"+ba);
            session.create(ba);
            session.create(billCycle);
            session.create(ratingCtxt);
            
            System.out.println("Set Rating Result :"+ratingCtxt);
            //BillingAccount readBa = _docDaoFactory.getDaoForClass(BillingAccount.class).get(ba.getKey());
            //System.out.println("Read Ba Result :"+readBa);
            //readBa.setLedgerSegment("Bis");
            attr.setCode("testing2");
            System.out.println("After Update Rating Result :" + ratingCtxt);
            billCycle.setEndDate(billCycle.getEndDate().plusMonths(1));
            System.out.println("After Update Billing Cycle Result :"+ba);

            StringCdrBucket cdrsBucket = new StringCdrBucket(GenericCdrsBucket.DocumentType.CDRS_BUCKET_FULL);
            cdrsBucket.setBillingAccountKey(ba.getKey());
            cdrsBucket.setBillingCycleKey(billCycle.getKey());
            cdrsBucket.setRatingContextKey(ratingCtxt.getKey());
            
            for(int i=0;i<5;++i){
                StringCdr cdr = new StringCdr("CDR_"+i);
                cdr.setCdrData("BaseCdrContent_"+i);
                cdrsBucket.addCdr(cdr);
            }
            
            _docDaoFactory.getDaoForClass(StringCdrBucket.class).create(cdrsBucket);
            
            GenericCdrsBucket<StringCdr> unpackedCdrsMap = _client.gets(cdrsBucket.getKey(),_docDaoFactory.getDaoForClass(StringCdrBucket.class).getTranscoder());
            
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
            
            _client.append(newCdrsBucket,_docDaoFactory.getDaoForClass(StringCdrBucket.class).getTranscoder()).get();
            unpackedCdrsMap = _client.gets(cdrsBucket.getKey(), _docDaoFactory.getDaoForClass(StringCdrBucket.class).getTranscoder());
            //System.out.println("Result :\n"+unpackedCdrsMap.toString());
            
            System.out.println("New Session");
            
            CouchbaseSession readSession=_docDaoFactory.newSession();
            BillingAccount readBa = readSession.get(ba.getKey(),BillingAccount.class);
            System.out.println("Ba Read finished");
            BillingCycle readCycle = readSession.get(billCycle.getKey(),BillingCycle.class);
            System.out.println("Cycle Read finished");
            System.out.println("Read Ba Result :"+readBa);
            System.out.println("Read BillCycle Result :"+readCycle);
            System.out.println("Read Cycle link :<"+readBa.getBillingCycleLinks().get(0).getLinkedObject(true)+">");
            
            */
            //bench();


        }
        catch(Exception e){
            e.printStackTrace();
        }
        _client.shutdown();
  }
    static Long counter;
    public static void bench(){

        ThreadPoolExecutor pool = new ThreadPoolExecutor(2,2,1,
                    TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(100,true),
                new ThreadPoolExecutor.CallerRunsPolicy());

        counter = 0L;
        long nbPut=0L;
        long startTime = System.currentTimeMillis();
        //Person person=new Person();

        //_docDaoFactory.getValidatorFactory().getValidator(person.getClass());
        //_docDaoFactory.getValidatorFactory().getValidator(Person.class);
        for(int i=0;i<10000;++i) {
            final int id=i;
            try {
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CouchbaseSession session = _sessionFactory.newReadWriteSession();
                            ProcessingServiceFactory serviceFactory = new ProcessingServiceFactory();

                            CreatePartyJob createPartyJob = session.newEntity(CreatePartyJob.class);
                            createPartyJob.getRequest().type = CreatePartyRequest.Type.person;
                            createPartyJob.getRequest().person = new CreatePartyRequest.Person();
                            createPartyJob.getRequest().person.firstName = "christophe " + id;
                            //if(id%2==0) {
                                createPartyJob.getRequest().person.lastName = "jeunesse " + id;
                            //}
                            serviceFactory.getJobServiceForClass(CreatePartyJob.class).execute(createPartyJob);
                            CreateBillingAccountJob createBaJob = session.newEntity(CreateBillingAccountJob.class);
                            createBaJob.getRequest().billDay=2;
                            createBaJob.getRequest().partyId = ((CreatePartyJob.CreatePartyTask) createPartyJob.getTasks().get(0)).getDocument().getUid();
                            serviceFactory.getJobServiceForClass(CreateBillingAccountJob.class).execute(createBaJob);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        synchronized (counter){
                            counter++;
                            if(counter%100==0){
                                System.out.println("Reaching "+counter);
                            }
                        }
                    }
                });
                nbPut++;
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.MINUTES)) {
                // pool didn't terminate after the first try
                pool.shutdownNow();
            }
            else {
                Long endTime = System.currentTimeMillis();
                System.out.println("Duration : "+((endTime-startTime)*1.0/1000));
                System.out.println("Avg Throughput : "+(nbPut/((endTime-startTime)*1.0/1000)));
            }

             if (!pool.awaitTermination(1, TimeUnit.MINUTES)) {
                // pool didn't terminate after the second try
             }
        } catch (InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    /*public static void bench(){
        CouchbaseSession benchSession=_docDaoFactory.newSession();
        //Tries to create 1 Ba
        int nbBa = 10000;
        List<BillingAccount> bas = new ArrayList<BillingAccount>(nbBa);
        List<BillingCycle> billingCycles = new ArrayList<BillingCycle>(nbBa);
        List<StandardRatingContext> ratCtxts = new ArrayList<StandardRatingContext>(nbBa);


        for(int i=0;i<nbBa;++i){
            BillingAccount baBench = benchSession.newEntity(BillingAccount.class);
            baBench.setLedgerSegment("test");
            BillingCycle billCycleBench =  benchSession.newEntity(BillingCycle.class);
            billCycleBench.setBillingAccount(baBench);
            billCycleBench.setStartDate((new DateTime()).withTime(0,0,0,0));
            billCycleBench.setEndDate(billCycleBench.getStartDate().plusMonths(1));

            StandardRatingContext ratingCtxtBench = benchSession.newEntity(StandardRatingContext.class);
            ratingCtxtBench.setBillingCycle(billCycleBench);
            RatingContextAttribute attrBench =  new RatingContextAttribute();
            ratingCtxtBench.addAttribute(attrBench);
            attrBench.setCode("testing");


            bas.add(baBench);
            billingCycles.add(billCycleBench);
            ratCtxts.add(ratingCtxtBench);
            if(i%10==0) System.out.println("Nb Created "+i);
        }
        System.out.println("Starting bench");
        long startTime = System.currentTimeMillis();
        benchSession.create(bas,BillingAccount.class);
        benchSession.create(billingCycles,BillingCycle.class);
        benchSession.create(ratCtxts,StandardRatingContext.class);
        System.out.println("Duration : "+((System.currentTimeMillis()-startTime)*1.0/1000));
    }*/
}
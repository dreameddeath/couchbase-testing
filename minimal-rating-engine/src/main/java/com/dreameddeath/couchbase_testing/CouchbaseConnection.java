/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.couchbase_testing;

import com.couchbase.client.java.CouchbaseCluster;
import com.dreameddeath.billing.dao.BillingAccountDao;
import com.dreameddeath.billing.dao.BillingCycleDao;
import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.cycle.BillingCycle;
import com.dreameddeath.billing.process.CreateBillingAccountJob;
import com.dreameddeath.core.CouchbaseSession;
import com.dreameddeath.core.CouchbaseSessionFactory;
import com.dreameddeath.core.couchbase.BinarySerializer;
import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.couchbase.GenericTranscoder;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.dao.process.JobDao;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.dao.validation.ValidatorFactory;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.model.binary.BinaryCouchbaseDocument;
import com.dreameddeath.core.model.document.BucketDocument;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.service.ProcessingServiceFactory;
import com.dreameddeath.core.user.User;
import com.dreameddeath.installedbase.process.CreateUpdateInstalledBaseJob;
import com.dreameddeath.party.dao.PartyDao;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.model.process.CreatePartyRequest;
import com.dreameddeath.party.process.CreatePartyJob;
import com.dreameddeath.rating.dao.RatingContextDao;
import com.dreameddeath.rating.model.cdr.GenericCdr;
import com.dreameddeath.rating.model.cdr.GenericCdrsBucket;
import com.dreameddeath.rating.model.context.RatingContext;
import com.dreameddeath.rating.storage.GenericCdrsBucketTranscoder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CouchbaseConnection {
    protected static final CouchbaseBucketWrapper _client;
    static{

        try{
            _client = new CouchbaseBucketWrapper(CouchbaseCluster.create("127.0.0.1"),"test","adminuser");
        }
        catch(Exception e){
            throw new RuntimeException("Init error",e);
        }
        

    }

    private static final CouchbaseCounterDaoFactory _counterDaoFactory = new CouchbaseCounterDaoFactory();
    private static final CouchbaseUniqueKeyDaoFactory _uniqueKeyDaoFactory=new CouchbaseUniqueKeyDaoFactory();
    private static final BaseCouchbaseDocumentDaoFactory _docDaoFactory = new BaseCouchbaseDocumentDaoFactory();

    static {
        _docDaoFactory.setCounterDaoFactory(_counterDaoFactory);
        _docDaoFactory.setValidatorFactory(new ValidatorFactory());
        _uniqueKeyDaoFactory.addDaoFor("personName", new CouchbaseUniqueKeyDao(_client,_uniqueKeyDaoFactory));
        _uniqueKeyDaoFactory.addDaoFor("createInstalledBaseJob", new CouchbaseUniqueKeyDao(_client,_uniqueKeyDaoFactory));
        _docDaoFactory.addDaoFor(BillingAccount.class, new BillingAccountDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(BillingCycle.class, new BillingCycleDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(RatingContext.class, new RatingContextDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(StringCdrBucket.class, new StringCdrBucketDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(Party.class, new PartyDao(_client, _docDaoFactory));
        _docDaoFactory.addDaoFor(AbstractJob.class, new JobDao(_client, _docDaoFactory));
    }
    private static final DateTimeServiceFactory _dateTimeServiceFactory = new DateTimeServiceFactory();
    private static final CouchbaseSessionFactory _sessionFactory = new CouchbaseSessionFactory(_docDaoFactory,_counterDaoFactory,_uniqueKeyDaoFactory,_dateTimeServiceFactory);
    
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
        public StringCdrBucket(BinaryCouchbaseDocument.BinaryDocumentType docType){ super(docType); }
        public StringCdrBucket(Integer origDbSize,BinaryCouchbaseDocument.BinaryDocumentType documentType){ super(origDbSize,documentType); }
    }
    
    // public static CdrsBucketLink<GenericCdrsBucket> buildLink(T genCdrsBucket){
        // CdrsBucketLink<T> newLink = new CdrsBucketLink<T>();
        // newLink.setDocumentKey(genCdrsBucket.getDocumentKey());
        // newLink.setType(genCdrsBucket.getClass().getSimpleName());
        // newLink.updateFromBucket(genCdrsBucket);
        // newLink.setLinkedObject(genCdrsBucket);
        // return newLink;
    // }
    public static class StringCdrRatingTrancoder extends GenericCdrsBucketTranscoder<StringCdr,StringCdrBucket>{
        @Override
        protected StringCdr genericCdrBuilder(String uid){ return new StringCdr(uid); }

        @Override
        protected StringCdrBucket genericCdrBucketBuilder(BinaryCouchbaseDocument.BinaryDocumentType docType){ return new StringCdrBucket(docType); }

        public StringCdrRatingTrancoder(Class<StringCdrBucket> clazz, Class<? extends BucketDocument<StringCdrBucket>> baseDocumentClazz){ super(clazz,baseDocumentClazz);}
    }
    
    
    
    public static class StringCdrBucketDao extends BaseCouchbaseDocumentDao<StringCdrBucket> {
        public static final String CDR_BUCKET_CNT_KEY="%s/cdrs/cnt";
        public static final String CDR_BUCKET_FMT_KEY="%s/cdrs/%d";
        public static final String CDR_BUCKET_KEY_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cdrs/\\d+";
        public static final String CDR_BUCKET_CNT_PATTERN=BillingAccountDao.BA_KEY_PATTERN+"/cdrs/cnt";

        public static class LocalBucketDocument extends BucketDocument<StringCdrBucket> {
            public LocalBucketDocument(StringCdrBucket obj){super(obj);}
        }

        private static StringCdrRatingTrancoder _tc = new StringCdrRatingTrancoder(StringCdrBucket.class,StringCdrBucketDao.LocalBucketDocument.class);
    
        public GenericTranscoder<StringCdrBucket> getTranscoder(){
            return _tc;
        }
       
        public StringCdrBucketDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
            super(client,factory);
            registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(CDR_BUCKET_CNT_PATTERN).withDefaultValue(1L));
        }

        public void buildKey(StringCdrBucket obj) throws DaoException{
            long result = obj.getBaseMeta().getSession().incrCounter(String.format(CDR_BUCKET_CNT_KEY, obj.getBillingAccountKey()), 1);
            obj.getBaseMeta().setKey(String.format(CDR_BUCKET_FMT_KEY, obj.getBillingAccountKey(), result));
        }

        public String getKeyPattern(){
            return CDR_BUCKET_KEY_PATTERN;
        }
    }
    
    public static void main(String[] args) throws Exception {
        //_client.getClient().flush().get();
        _client.start();
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

            CreatePartyJob readJob = (CreatePartyJob)session.get(createPartyJob.getDocumentKey());
            System.out.println("Job <"+readJob.getDocumentKey()+"> status <"+readJob.getDocState()+">");
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
            //BillingAccount readBa = _docDaoFactory.getDaoForClass(BillingAccount.class).get(ba.getDocumentKey());
            //System.out.println("Read Ba Result :"+readBa);
            //readBa.setLedgerSegment("Bis");
            attr.setCode("testing2");
            System.out.println("After Update Rating Result :" + ratingCtxt);
            billCycle.setEndDate(billCycle.getEndDate().plusMonths(1));
            System.out.println("After Update Billing Cycle Result :"+ba);

            StringCdrBucket cdrsBucket = new StringCdrBucket(GenericCdrsBucket.BinaryDocumentType.CDRS_BUCKET_FULL);
            cdrsBucket.setBillingAccountKey(ba.getDocumentKey());
            cdrsBucket.setBillingCycleKey(billCycle.getDocumentKey());
            cdrsBucket.setRatingContextKey(ratingCtxt.getDocumentKey());
            
            for(int i=0;i<5;++i){
                StringCdr cdr = new StringCdr("CDR_"+i);
                cdr.setCdrData("BaseCdrContent_"+i);
                cdrsBucket.addCdr(cdr);
            }
            
            _docDaoFactory.getDaoForClass(StringCdrBucket.class).create(cdrsBucket);
            
            GenericCdrsBucket<StringCdr> unpackedCdrsMap = _client.get(cdrsBucket.getDocumentKey(),_docDaoFactory.getDaoForClass(StringCdrBucket.class).getTranscoder());
            
            StringCdrBucket newCdrsBucket = new StringCdrBucket(unpackedCdrsMap.getDocumentKey(),unpackedCdrsMap.getDocumentDbSize(),GenericCdrsBucket.BinaryDocumentType.CDRS_BUCKET_PARTIAL_WITH_CHECKSUM);
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
            unpackedCdrsMap = _client.get(cdrsBucket.getDocumentKey(), _docDaoFactory.getDaoForClass(StringCdrBucket.class).getTranscoder());
            //System.out.println("Result :\n"+unpackedCdrsMap.toString());
            
            System.out.println("New Session");
            
            CouchbaseSession readSession=_docDaoFactory.newSession();
            BillingAccount readBa = readSession.get(ba.getDocumentKey(),BillingAccount.class);
            System.out.println("Ba Read finished");
            BillingCycle readCycle = readSession.get(billCycle.getDocumentKey(),BillingCycle.class);
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
    static Long errorCounter;
    public static void bench(){

        ThreadPoolExecutor pool = new ThreadPoolExecutor(2,4,1,
                    TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(100,true),
                new ThreadPoolExecutor.CallerRunsPolicy());

        counter = 0L;
        errorCounter=0L;
        long nbPut=0L;
        long startTime = System.currentTimeMillis();

        final ProcessingServiceFactory serviceFactory = new ProcessingServiceFactory();

        for(int i=0;i<20000;++i) {
            final int id=i;
            try {
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CouchbaseSession session = _sessionFactory.newReadWriteSession(new User() {
                                @Override
                                public String getUserId() {
                                    return null;
                                }

                                @Override
                                public Boolean hasRight(String namme) {
                                    return null;
                                }

                                @Override
                                public String getProperty(String name) {
                                    return null;
                                }
                            });

                            CreatePartyJob createPartyJob = session.newEntity(CreatePartyJob.class);
                            createPartyJob.getRequest().type = CreatePartyRequest.Type.person;
                            createPartyJob.getRequest().person = new CreatePartyRequest.Person();
                            createPartyJob.getRequest().person.firstName = "christophe " + id;
                            createPartyJob.getRequest().person.lastName = "jeunesse " + (id);
                            serviceFactory.execute(createPartyJob);

                            CreateBillingAccountJob createBaJob = session.newEntity(CreateBillingAccountJob.class);
                            createBaJob.getRequest().billDay=2;
                            createBaJob.getRequest().partyId = ((CreatePartyJob.CreatePartyTask) createPartyJob.getTasks().get(0)).getDocument().getUid();
                            serviceFactory.execute(createBaJob);

                            CreateUpdateInstalledBaseJob createInstalledBaseJob = session.newEntity(CreateUpdateInstalledBaseJob.class);
                            createInstalledBaseJob.getRequest().creationRequestUid = "newInstalledBase "+ id;
                            serviceFactory.execute(createInstalledBaseJob);

                            session.delete(createInstalledBaseJob);

                        } catch (Exception e) {
                            e.printStackTrace();
                            synchronized (errorCounter){
                                errorCounter++;
                            }
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
        System.out.println("Having "+errorCounter+" errors");
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
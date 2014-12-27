package com.dreameddeath.core.test;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.view.*;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.storage.ICouchbaseBucket;
import com.dreameddeath.core.storage.impl.CouchbaseBucketSimulator;
import com.dreameddeath.core.storage.impl.CouchbaseBucketWrapper;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ceaj8230 on 18/12/2014.
 */
public class Utils {
    public static class TestEnvironment{
        public static final String TESTING_DESIGN_DOC = "TESTING_TOOL";
        public static final String VIEW_PER_PREFIX = "perPrefixView";
        private CouchbaseSessionFactory _sessionFactory ;
        private ICouchbaseBucket _client;


        public TestEnvironment(String prefix){
            if(
                    (System.getenv("COUCHBASE_BUCKET_NAME")!=null) &&
                            (System.getenv("COUCHBASE_BUCKET_PASSWORD")!=null) &&
                            (System.getenv("COUCHBASE_DB_URL")!=null)
                    )
            {
                _client = new CouchbaseBucketWrapper(CouchbaseCluster.create(System.getenv("COUCHBASE_DB_URL")),System.getenv("COUCHBASE_BUCKET_NAME"),System.getenv("COUCHBASE_BUCKET_PASSWORD"),prefix);
            }
            else{
                _client = new CouchbaseBucketSimulator("test",prefix);
            }
            _sessionFactory = (new CouchbaseSessionFactory.Builder()).build();
            _sessionFactory.getUniqueKeyDaoFactory().setDefaultTranscoder(new GenericJacksonTranscoder<>(CouchbaseUniqueKey.class));
        }

        public <TOBJ extends CouchbaseDocument> void addDocumentDao(CouchbaseDocumentDao dao,Class<TOBJ> objClass){
            _sessionFactory.getDocumentDaoFactory().addDao(dao.setClient(_client), new GenericJacksonTranscoder<>(objClass));
        }

        public Map<String,String> testingUtilsViews(){
            Map<String,String> listViews = new HashMap<>();
            listViews.put(VIEW_PER_PREFIX,
                    "function(doc,meta){\n"+
                    "   var prefix = /^(\\w+)\\$/.exec(meta.id);\n"+
                    "   if(prefix!==null){\n"+
                    "      emit(prefix[1],null);\n"+
                    "    }\n"+
                    "}"
                    );
            return listViews;
        }

        public void start() throws StorageException{
            _client.start();
            if(_client.getClass().equals(CouchbaseBucketWrapper.class)){
                Bucket bucket=((CouchbaseBucketWrapper) _client).getBucket();
                DesignDocument designDocument = bucket.bucketManager().getDesignDocument(TESTING_DESIGN_DOC);
                Map<String,String> referenceMap=testingUtilsViews();
                boolean toRebuild = false;
                if((designDocument ==null) || (referenceMap.values().size()!=designDocument.views().size())){
                    toRebuild = true;
                }
                else for(View view:designDocument.views()){
                    if(!referenceMap.containsKey(view.name())){
                        toRebuild = true;
                    }
                    else if(!referenceMap.get(view.name()).equals(view.map())){
                        toRebuild = true;
                    }
                    if(toRebuild) break;
                }

                if(toRebuild){
                    List<View> listView = new ArrayList<>();
                    for(Map.Entry<String,String> entry:referenceMap.entrySet()){
                        listView.add(DefaultView.create(entry.getKey(),entry.getValue()));
                    }
                    DesignDocument newDesignDocument = DesignDocument.create(TESTING_DESIGN_DOC,listView);

                    bucket.bucketManager().upsertDesignDocument(newDesignDocument);
                }
            }
            _sessionFactory.getDocumentDaoFactory().getViewDaoFactory().initAllViews();
        }

        public CouchbaseSessionFactory getSessionFactory(){return _sessionFactory;}

        public void shutdown(boolean cleanUp){
            if(cleanUp && _client.getClass().equals(CouchbaseBucketWrapper.class)){
                ViewQuery listQuery = ViewQuery.from(TESTING_DESIGN_DOC,VIEW_PER_PREFIX).key(_client.getPrefix());
                listQuery.stale(Stale.TRUE);
                final Bucket bucket=((CouchbaseBucketWrapper) _client).getBucket();
                bucket.async().query(listQuery).
                        flatMap(
                                AsyncViewResult::rows
                        ).
                        flatMap(
                                aViewRow ->
                                bucket.async().remove(aViewRow.id())
                        ).toBlocking().last();
            }
            _client.shutdown();
        }

    }


}

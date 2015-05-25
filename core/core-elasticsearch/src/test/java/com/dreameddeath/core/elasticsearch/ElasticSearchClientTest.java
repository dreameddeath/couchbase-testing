package com.dreameddeath.core.elasticsearch;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.transcoder.json.GenericJacksonTranscoder;
import com.dreameddeath.testing.elasticsearch.ElasticSearchServer;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

import static org.junit.Assert.assertEquals;

/**
 * Created by CEAJ8230 on 25/05/2015.
 */
public class ElasticSearchClientTest {
    public static final String CLUSTER_NAME = "testEsClient";
    public static final String INDEX_NAME = "test";
    ElasticSearchServer _server;

    public class TestDoc extends CouchbaseDocument {
        @DocumentProperty("lastName")
        public String lastName;

        @DocumentProperty("firstName")
        public String firstName;

    }


    @Before
    public void initServer()throws Exception{
        _server = new ElasticSearchServer(CLUSTER_NAME);
        _server.start();
        _server.createAndInitIndex(INDEX_NAME);
    }




    @Test
    public void testIndexingDocument() throws Exception{
        ElasticSearchClient client = new ElasticSearchClient(_server.getClient(), GenericJacksonTranscoder.MAPPER);
        TestDoc doc = new TestDoc();
        doc.firstName = "christophe";
        doc.lastName = "jeunesse";
        doc.getBaseMeta().setCas(1);
        doc.getBaseMeta().setKey("/test/1");
        {
            Observable<IndexResponse> createResponseObs = client.create(INDEX_NAME, "testDoc", doc);
            IndexResponse createResponse = createResponseObs.toBlocking().first();
            assertEquals(true, createResponse.isCreated());
        }
        doc.firstName="louise";
        doc.getBaseMeta().setKey("/test/2");
        {
            Observable<IndexResponse> createResponseObs = client.create(INDEX_NAME, "testDoc", doc);
            IndexResponse createResponse = createResponseObs.toBlocking().first();
            assertEquals(true, createResponse.isCreated());
        }
        doc.lastName="leguen";
        doc.firstName="barbara";
        doc.getBaseMeta().setKey("/test/3");
        {
            Observable<IndexResponse> createResponseObs = client.create(INDEX_NAME, "testDoc", doc);
            IndexResponse createResponse = createResponseObs.toBlocking().first();
            assertEquals(true, createResponse.isCreated());
        }

        //Wait for indexing
        Thread.sleep(2000);

        SearchResponse searchResponse = client.getClient().prepareSearch(INDEX_NAME).setTypes("testDoc").setQuery(QueryBuilders.matchQuery("lastName", "jeunesse")).execute().actionGet();
        assertEquals(2,searchResponse.getHits().getTotalHits());

    }

    @After
    public void closeServer(){
        _server.stop();
    }
}
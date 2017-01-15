/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.testing.elasticsearch;


import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Christophe Jeunesse on 25/05/2015.
 */
public class ElasticSearchServer implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchServer.class);
    private final File esHomeDir;
    private final Settings settings;
    private Node node=null;
    private Client client=null;

    public ElasticSearchServer(String clusterName) throws Exception{
        esHomeDir = Files.createTempDirectory("elasticsearch_home_"+clusterName).toFile();
        settings = Settings.builder()
                .put("path.home", esHomeDir.toString())
                .put("path.data", new File(esHomeDir,"data").toString())
                .put("cluster.name", clusterName)
                .put("transport.type","local")
                .put("http.enabled",false)
                .build();
    }

    public Node getNode(){
        if(node==null){
            node = new Node(settings);
            //node = NodeBuilder.nodeBuilder().local(true).settings(settings).build();
        }
        return node;
    }

    public Client getClient(){
        if(client==null){
            client = getNode().client();
        }
        return client;
    }

    public void start(){
        try {
            getNode().start();
        }
        catch(NodeValidationException e){
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        if(node!=null) {
            try {
                node.close();
                node = null;
            }
            catch(IOException e){
                LOG.warn("Cannot close",e);
            }
        }
        try{
            FileUtils.forceDelete(esHomeDir);
        }
        catch(IOException e){
            LOG.warn("Cannot cleanup",e);
            //Ignore error
        }
    }

    @Override
    public void close() throws IOException {
        this.stop();
    }

    public void createAndInitIndex(String indexName){
        synchronized (this) {
            getClient().admin().indices().prepareCreate(indexName).execute().actionGet();
            getClient().admin().cluster().prepareHealth(indexName).setWaitForActiveShards(1).execute().actionGet();
        }
    }

    public void syncIndexes(){
        synchronized (this) {
            getClient().admin().indices().prepareRefresh().execute().actionGet();
        }
    }
}

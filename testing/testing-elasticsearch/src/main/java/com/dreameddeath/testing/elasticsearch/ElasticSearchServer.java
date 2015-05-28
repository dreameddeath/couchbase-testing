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

package com.dreameddeath.testing.elasticsearch;


import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Christophe Jeunesse on 25/05/2015.
 */
public class ElasticSearchServer {

    private final File _dataDir;
    private final Settings _settings;
    private Node _node=null;
    private Client _client=null;

    public ElasticSearchServer(String clusterName) throws Exception{
        _dataDir = Files.createTempDirectory("elasticsearch_data_"+clusterName).toFile();
        _settings = ImmutableSettings.settingsBuilder()
                .put("path.data", _dataDir.toString())
                .put("cluster.name", clusterName)
                .build();
    }

    public Node getNode(){
        if(_node==null){
            _node = NodeBuilder.nodeBuilder().local(true).settings(_settings).build();
        }
        return  _node;
    }

    public Client getClient(){
        if(_client==null){
            _client = _node.client();
        }
        return _client;
    }

    public void start(){
        getNode().start();
    }

    public void stop(){
        if(_node!=null) {
            _node.close();
        }
        try{
            FileUtils.forceDelete(_dataDir);
        }
        catch(IOException e){
            //Ignore error
        }
    }

    public void createAndInitIndex(String indexName){
        getClient().admin().indices().prepareCreate(indexName).execute().actionGet();
        getClient().admin().cluster().prepareHealth(indexName).setWaitForActiveShards(1).execute().actionGet();
    }
}

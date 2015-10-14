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

package com.dreameddeath.infrastructure.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 29/11/2014.
 */
public class TestSpout implements IRichSpout {
    SpoutOutputCollector collector;
    private int pos=1;
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("test"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        //topologyContext.getRawTopology().
        //TOTO init view criterion
    }

    @Override
    public void close() {

    }

    @Override
    public void activate() {
        pos = 0;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public void nextTuple() {
        collector.emit(new Values("Test value " +(++pos)));
    }

    @Override
    public void ack(Object o) {

    }

    @Override
    public void fail(Object o) {

    }
}

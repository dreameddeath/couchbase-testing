package com.dreameddeath.infrastructure.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.util.Map;

/**
 * Created by ceaj8230 on 29/11/2014.
 */
public class TestSpout implements IRichSpout {
    SpoutOutputCollector _collector;
    private int _pos=1;
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
        _pos = 0;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public void nextTuple() {
        _collector.emit(new Values("Test value " +(++_pos)));
    }

    @Override
    public void ack(Object o) {

    }

    @Override
    public void fail(Object o) {

    }
}

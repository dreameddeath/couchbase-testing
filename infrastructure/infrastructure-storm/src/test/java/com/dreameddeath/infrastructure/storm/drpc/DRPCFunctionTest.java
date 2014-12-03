package com.dreameddeath.infrastructure.storm.drpc;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.generated.TopologyInfo;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.Function;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class DRPCFunctionTest {
    public static class TestingClass implements JSONAware{
        @JsonProperty
        public String in;
        @JsonProperty
        public String out;

        public TestingClass(String input,String output ){in=input;out=output;}
        public TestingClass(){}
        @Override
        public String toJSONString() {
            JSONObject result =new JSONObject();
            result.put("in",in);
            result.put("out",out);
            return result.toString();
        }
    }

    public static class ListResult extends ArrayList<ArrayList<TestingClass>>{}

    public static class TestStdFunction extends BaseFunction {
        private static Logger LOG = LoggerFactory.getLogger(TestStdFunction.class);
        @Override
        public void execute(TridentTuple tridentTuple, TridentCollector tridentCollector) {
            //LOG.warn("Trident data size <{}>", tridentTuple.size());
            //Map<String,TestingClass> map = new HashMap<>();
            String input = tridentTuple.getString(0);
            //map.put(input,new TestingClass(input,input + " from DRPC"));
            Values emitValue = new Values(new TestingClass(input,input + " from DRPC"));
            LOG.warn("Trident data size <{}>", emitValue);
            tridentCollector.emit(emitValue);
        }
    }

    public static class DRCPCallTestFunction extends DRPCFunction<String>{

        public DRCPCallTestFunction(String drcpServerName, String drcpFunctionName) {
            super(drcpServerName, drcpFunctionName);
        }

        /*public DRCPCallTestFunction(String drcpServerName, String drcpFunctionName,LocalDRPC drpc) {
            super(drcpServerName, drcpFunctionName,drpc);
        }*/

        @Override
        public void DRPCResultProcess(String output, TridentCollector collector) {
            collector.emit(new Values(output + " has worked"));
        }
    }

    @Test
    public void testDRCP(){
        LocalDRPC drpc = new LocalDRPC();
        LocalCluster cluster = new LocalCluster();

        Config conf = new Config();
        conf.setDebug(true);
        conf.put(DRPCFunction.builderServerConfigEntry("test", DRPCFunction.EntryType.SERVER_NAME),"toto.tutu.com");
        conf.put(DRPCFunction.builderServerConfigEntry("test", DRPCFunction.EntryType.SERVER_PORT),"1023");
        TridentTopology topology = new TridentTopology();
        topology.newDRPCStream("testSimpleFunction",drpc).parallelismHint(3).each(new Fields("args"), new TestStdFunction(), new Fields("result")).project(new Fields("result"));
        cluster.submitTopology("testSimpleFunction", conf, topology.build());

        /*TridentTopology topologyDRCP = new TridentTopology();
        topologyDRCP.newDRPCStream("testDRCP",drpc).each(
                new Fields("args"),
                new DRCPCallTestFunction("test", "testSimpleFunction"), new Fields("result")).project(new Fields("result"));
        cluster.submitTopology("testDRCPFunction", conf, topologyDRCP.build());*/
        String result = drpc.execute("testSimpleFunction","the value submitted");

        System.out.println("The found element is :"+result);
        try {
            ListResult parsingResult = new ObjectMapper().readValue(result,ListResult.class);
            Object raw = new JSONParser().parse(result);
            assertEquals("the value submitted from DRPC",parsingResult.get(0).get(0).out);
        }
        catch(ParseException|IOException e){
            fail("error during parsing :"+e.getMessage());
        }
    }

}
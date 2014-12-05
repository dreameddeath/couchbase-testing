package com.dreameddeath.infrastructure.storm.drpc;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

    public static class TestStdFunction extends BaseFunction {
        private static Logger LOG = LoggerFactory.getLogger(TestStdFunction.class);
        @Override
        public void execute(TridentTuple tridentTuple, TridentCollector tridentCollector) {
            String input = tridentTuple.getString(0);
            try {
                TestingClass result = new ObjectMapper().readValue(tridentTuple.getString(0), TestingClass.class);
                result.out+=" from DRPC";
                Values emitValue = new Values(result);
                tridentCollector.emit(emitValue);
            }
            catch(Exception e){
                throw new RuntimeException("Error ",e);
            }
        }
    }

    public static class DRPCCallTestFunction extends DRPCFunction<TestingClass,TestingClass>{
        public DRPCCallTestFunction(String drpcServerName, String drpcFunctionName) {
            super(drpcServerName, drpcFunctionName);
        }

        @Override
        public TestingClass DRPCInputPrepareProcess(TridentTuple tridentTupl,ObjectMapper mapper) {
            try {
                return mapper.readValue(tridentTupl.getString(0), TestingClass.class);
            }
            catch(Exception e){
                throw new RuntimeException("Error during parsing of tuple <"+tridentTupl.toString()+">",e);
            }
        }

        @Override
        public TypeReference<List<List<TestingClass>>> getTypeReference() {
            return new TypeReference<List<List<TestingClass>>>(){};
        }


        @Override
        public void DRPCResultProcess(TestingClass output, TridentCollector collector) {
            try {
                output.out += " has worked";
                collector.emit(new Values(output));
            }
            catch(Exception e){
                throw new RuntimeException("error during output submit",e);
            }
        }
    }

    @Test
    public void testDRPC(){
        LocalDRPC drpc = new LocalDRPC();
        LocalCluster cluster = new LocalCluster();

        Config conf = new Config();
        conf.setDebug(true);
        conf.put(DRPCFunction.builderServerConfigEntry("test", DRPCFunction.EntryType.SERVER_NAME),"toto.tutu.com");
        conf.put(DRPCFunction.builderServerConfigEntry("test", DRPCFunction.EntryType.SERVER_PORT),"1023");
        conf.put(DRPCFunction.builderServerConfigEntry("test", DRPCFunction.EntryType.LOCAL_SERVICE_ID),drpc.getServiceId());
        TridentTopology topology = new TridentTopology();
        topology.newDRPCStream("testSimpleFunction",drpc).parallelismHint(3).each(new Fields("args"), new TestStdFunction(), new Fields("result")).project(new Fields("result"));
        cluster.submitTopology("testSimpleFunction", conf, topology.build());

        TridentTopology topologyDRPC = new TridentTopology();
        topologyDRPC.newDRPCStream("testDRPCFunction", drpc).each(
                new Fields("args"),
                new DRPCCallTestFunction("test", "testSimpleFunction"), new Fields("result")).project(new Fields("result"));
        cluster.submitTopology("testDRPCFunction", conf, topologyDRPC.build());

        TestingClass input = new TestingClass();
        input.in = "the intial value submitted";
        input.out = "the intial value submitted";

        try {
            //String result = drpc.execute("testSimpleFunction",new ObjectMapper().writeValueAsString(input));

            String result2 = drpc.execute("testDRPCFunction",new ObjectMapper().writeValueAsString(input));
            System.out.println(result2);




            System.out.println("The found element is :"+result2);

            //ListResult parsingResult = new ObjectMapper().readValue(result,ListResult.class);
            List<List<TestingClass>> parsingResult = new ObjectMapper().readValue(
                    result2,
                    new TypeReference<List<List<TestingClass>>>() {}
                );
            //Object raw = new JSONParser().parse(result);
            assertEquals("the intial value submitted from DRPC has worked",parsingResult.get(0).get(0).out);
        }
        catch(IOException e){
            fail("error during parsing :"+e.getMessage());
        }
    }

}
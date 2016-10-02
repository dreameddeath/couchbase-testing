/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.infrastructure.storm.drpc;

import com.dreameddeath.testing.curator.CuratorTestUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.shade.org.json.simple.JSONAware;
import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DRPCFunctionTest {
    public static class TestingClass implements JSONAware {
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
                TestingClass result = new ObjectMapper().readValue(input, TestingClass.class);
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


    CuratorTestUtils  curatorTestUtils;
    @Before
    public void init() throws Exception{
        curatorTestUtils = new CuratorTestUtils();
        curatorTestUtils.prepare(2);
    }

    @Test
    public void testDRPC() throws Exception{
        //Config.STORM_ZOOKEEPER_SERVERS;
        LocalDRPC drpc = new LocalDRPC();
        String[] parts = curatorTestUtils.getCluster().getServers().get(0).getInstanceSpec().getConnectString().split(":");
        LocalCluster cluster = new LocalCluster(parts[0],Long.parseLong(parts[1]));

        Config conf = new Config();
        conf.setDebug(true);
        conf.put(DRPCFunction.builderServerConfigEntry("test", DRPCFunction.EntryType.SERVER_NAME), "toto.tutu.com");
        conf.put(DRPCFunction.builderServerConfigEntry("test", DRPCFunction.EntryType.SERVER_PORT), "1023");
        conf.put(DRPCFunction.builderServerConfigEntry("test", DRPCFunction.EntryType.LOCAL_SERVICE_ID), drpc.getServiceId());
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

        cluster.shutdown();
        drpc.shutdown();
    }

    @After
    public void end()throws Exception{
        if(curatorTestUtils!=null){
            curatorTestUtils.stop();
        }
    }
}
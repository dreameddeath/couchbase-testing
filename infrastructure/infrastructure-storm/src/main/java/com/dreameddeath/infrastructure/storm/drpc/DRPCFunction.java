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

package com.dreameddeath.infrastructure.storm.drpc;


import backtype.storm.generated.DistributedRPC;
import backtype.storm.utils.DRPCClient;
import backtype.storm.utils.ServiceRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.tuple.TridentTuple;

import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 02/12/2014.
 */
public abstract class DRPCFunction<TIN,TOUT> extends BaseFunction {
    private static ObjectMapper MAPPER = new ObjectMapper();

    private static Logger LOG = LoggerFactory.getLogger(DRPCFunction.class);
    private DistributedRPC.Iface drpcClient;
    private String drpcServerName;
    private String drpcFunctionName;

    public DRPCFunction(String drpcServerName, String drpcFunctionName){
        this.drpcServerName = drpcServerName;
        this.drpcFunctionName = drpcFunctionName;
    }

    @Override
    public void prepare(Map conf, TridentOperationContext context) {
        super.prepare(conf,context);
        //LOG.warn(conf.toString());
        boolean isLocal = "local".equals(conf.get("storm.cluster.mode"));
        String serverHost = (String)conf.get(builderServerConfigEntry(drpcServerName,EntryType.SERVER_NAME));
        Integer serverPort = Integer.parseInt((String) conf.get(builderServerConfigEntry(drpcServerName, EntryType.SERVER_PORT)));
        String localServiceId = (String)conf.get(builderServerConfigEntry(drpcServerName,EntryType.LOCAL_SERVICE_ID));
        LOG.warn("isLocal {} with service id {}",isLocal,localServiceId);
        if(isLocal) {
            drpcClient =  (DistributedRPC.Iface) ServiceRegistry.getService(localServiceId);
        }
        else{
            drpcClient = new DRPCClient(serverHost,serverPort);
        }
    }

    public abstract TIN DRPCInputPrepareProcess(TridentTuple tridentTuple,ObjectMapper mapper);
    public abstract TypeReference<List<List<TOUT>>> getTypeReference();

    @Override
    public void execute(TridentTuple tridentTuple, TridentCollector tridentCollector) {
        try {
            TIN processedInput=DRPCInputPrepareProcess(tridentTuple,MAPPER);
            String drpcStrInput = MAPPER.writeValueAsString(processedInput);
            String outputResult = drpcClient.execute(drpcFunctionName,drpcStrInput);
            List<List<TOUT>> parsingResult = MAPPER.readValue(outputResult,getTypeReference());

            DRPCResultProcess(parsingResult.get(0).get(0), tridentCollector);

        }
        catch(Exception e){
            LOG.error("Error",e);
            throw new RuntimeException("Error during DRPC call ",e);
        }
    }

    public abstract void DRPCResultProcess(TOUT output, TridentCollector collector);


    public static String builderServerConfigEntry(String serverName,EntryType type){
        switch(type){
            case SERVER_NAME:return "drpc.server."+ serverName +".location";
            case SERVER_PORT:return "drpc.server."+ serverName +".port";
            case LOCAL_SERVICE_ID:return "drpc.server."+ serverName +".local_service_id";
            default : throw new RuntimeException("Error parameter unknown "+type);
        }
    }

    public enum EntryType{
        SERVER_NAME,
        SERVER_PORT,
        LOCAL_SERVICE_ID
    }
}

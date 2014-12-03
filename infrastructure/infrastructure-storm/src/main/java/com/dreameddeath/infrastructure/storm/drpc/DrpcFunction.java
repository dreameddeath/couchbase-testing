package com.dreameddeath.infrastructure.storm.drpc;

import backtype.storm.utils.DRPCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.tuple.TridentTuple;

import java.util.Map;

/**
 * Created by CEAJ8230 on 02/12/2014.
 */
public abstract class DRPCFunction<T> extends BaseFunction {
    private static Logger LOG = LoggerFactory.getLogger(DRPCFunction.class);
    private DRPCClient _drpcClient;
    private String _drpcServerName;
    private String _drpcFunctionName;

    public DRPCFunction(String drpcServerName, String drpcFunctionName){
        _drpcServerName = drpcServerName;
        _drpcFunctionName = drpcFunctionName;
    }

    @Override
    public void prepare(Map conf, TridentOperationContext context) {
        super.prepare(conf,context);
        //LOG.warn(conf.toString());
        boolean isLocal = "local".equals(conf.get("storm.cluster.mode"));
        String serverHost = (String)conf.get(builderServerConfigEntry(_drpcServerName,EntryType.SERVER_NAME));
        Integer serverPort = Integer.parseInt((String) conf.get(builderServerConfigEntry(_drpcServerName, EntryType.SERVER_PORT)));
        //LG.warn("isLocal {}",isLocal);
        /*if(isLocal) {
            _drpcClient = new DRPCClient("localhost", 3773);//drpc.invocations.port
        }*/
        if(isLocal){
            _drpcClient = new DRPCClient(serverHost, serverPort);
        }
    }


    @Override
    public void execute(TridentTuple tridentTuple, TridentCollector tridentCollector) {
        try {
            DRPCResultProcess((T) _drpcClient.execute(_drpcFunctionName, tridentTuple.getString(0)), tridentCollector);
        }
        catch(Exception e){
            LOG.error("Error",e);
            throw new RuntimeException("Error during DRPC call ",e);
        }
    }

    public abstract void DRPCResultProcess(T output, TridentCollector collector);


    public static String builderServerConfigEntry(String serverName,EntryType type){
        switch(type){
            case SERVER_NAME:return "drpc.server."+ serverName +".location";
            case SERVER_PORT:return "drpc.server."+ serverName +".port";
            default : throw new RuntimeException("Error parameter unknown "+type);
        }
    }

    public enum EntryType{
        SERVER_NAME,
        SERVER_PORT
    }
}

package com.dreameddeath.core.exception.curator;

import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * Created by CEAJ8230 on 06/02/2015.
 */
public class DuplicateClusterClientException extends Exception {
    private List<String> _connectionString;
    private CuratorFramework _existingFramework;

    public DuplicateClusterClientException(List<String> connections,String message,CuratorFramework existingClient){
        super(message);
        _connectionString = connections;
        _existingFramework = existingClient;
    }

    public CuratorFramework getExistingFramework(){
        return _existingFramework;
    }

}

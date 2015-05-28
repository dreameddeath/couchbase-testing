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

package com.dreameddeath.core.curator.exception;

import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 06/02/2015.
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

/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.process.discovery;

import com.dreameddeath.core.curator.discovery.impl.StandardCuratorDiscoveryImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.process.model.discovery.TaskExecutorClientInfo;
import com.dreameddeath.core.process.registrar.TaskExecutorClientRegistrar;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 11/03/2016.
 */
public class TaskProcessClientDiscovery extends StandardCuratorDiscoveryImpl<TaskExecutorClientInfo> {
    private final ObjectMapper mapper= ObjectMapperFactory.BASE_INSTANCE.getMapper();

    public TaskProcessClientDiscovery(CuratorFramework curatorFramework) {
        super(curatorFramework, TaskExecutorClientRegistrar.getRootPath());
    }

    @Override
    protected TaskExecutorClientInfo deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, TaskExecutorClientInfo.class);
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public Set<String> getDomains(){
        return getList().stream().map(TaskExecutorClientInfo::getDomain).collect(Collectors.toSet());
    }

    public List<TaskExecutorClientInfo> getList(String domain){
        return getList().stream().filter(taskInfo->domain.equals(taskInfo.getDomain())).collect(Collectors.toList());
    }
}

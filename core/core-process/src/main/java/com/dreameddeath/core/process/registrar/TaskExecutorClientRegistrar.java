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

package com.dreameddeath.core.process.registrar;

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.curator.registrar.impl.CuratorRegistrarImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.process.config.ProcessConfigProperties;
import com.dreameddeath.core.process.model.discovery.TaskExecutorClientInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by Christophe Jeunesse on 10/03/2016.
 */
public class TaskExecutorClientRegistrar extends CuratorRegistrarImpl<TaskExecutorClientInfo> {
    public static String getRootPath(){
        try {
            return ProcessConfigProperties.TASKS_REGISTER_PATH.getMandatoryValue("The taskClient registering path isn't set");
        }
        catch (ConfigPropertyValueNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();
    private final String daemonUid;
    private final String webServerUid;

    public TaskExecutorClientRegistrar(CuratorFramework curatorFramework,String daemonUid,String webServerUid) {
        super(curatorFramework, getRootPath());
        this.daemonUid = daemonUid;
        this.webServerUid= webServerUid;
    }


    public void enrich(TaskExecutorClientInfo obj){
        obj.setDaemonUid(daemonUid);
        obj.setWebServerUid(webServerUid);
    }


    @Override
    protected byte[] serialize(TaskExecutorClientInfo obj) throws Exception {
        return mapper.writeValueAsBytes(obj);
    }
}

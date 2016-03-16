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

package com.dreameddeath.core.process.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by Christophe Jeunesse on 10/03/2016.
 */
@ConfigPropertyPackage(domain = "core",name = "processes",descr = "Configuration properties for processes")
public class ProcessConfigProperties {

    @ConfigPropertyDoc(
            name="core.process.job.register.path",
            descr = "defines the zookeeper registering path",
            defaultValue = "processes/clients/job",
            examples = {"processes/clients/job"}
    )
    public static final StringConfigProperty JOB_REGISTER_PATH = ConfigPropertyFactory.getStringProperty("core.process.job.register.path","jobClients/instances");


    @ConfigPropertyDoc(
            name="core.process.task.register.path",
            descr = "defines the zookeeper registering path",
            defaultValue = "processes/clients/task",
            examples = {"processes/clients/task"}
    )
    public static final StringConfigProperty TASKS_REGISTER_PATH = ConfigPropertyFactory.getStringProperty("core.process.task.register.path","taskClients/instances");


}

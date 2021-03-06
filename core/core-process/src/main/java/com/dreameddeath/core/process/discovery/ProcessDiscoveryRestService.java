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

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.process.model.discovery.JobExecutorClientInfo;
import com.dreameddeath.core.process.model.discovery.TaskExecutorClientInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Christophe Jeunesse on 11/03/2016.
 */
@Path("/processors")
@Api(value = "/processors", description = "List of registered processors")
public class ProcessDiscoveryRestService {
    private JobProcessClientDiscovery jobDiscovery;
    private TaskProcessClientDiscovery taskDiscovery;

    @Autowired
    public void setJobDiscovery(JobProcessClientDiscovery jobDiscovery) {
        this.jobDiscovery = jobDiscovery;
    }

    @Autowired
    public void setTaskDiscovery(TaskProcessClientDiscovery taskDiscovery) {
        this.taskDiscovery = taskDiscovery;
    }

    @GET
    @Path("domains")
    public Set<String> getDomains(){
        Set<String> domains = new TreeSet<>(jobDiscovery.getDomains());
        domains.addAll(taskDiscovery.getDomains());
        return domains;
    }

    @GET
    @Path("jobs")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<JobExecutorClientInfo> getAllJobClients(@QueryParam("domain") String domain){
        if(StringUtils.isEmpty(domain)) {
            return jobDiscovery.getList();
        }
        else{
            return jobDiscovery.getList(domain);
        }
    }

    @GET
    @Path("jobs/{uid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public JobExecutorClientInfo getJobClient(@PathParam("uid") String uid){
        return jobDiscovery.get(uid);
    }

    @GET
    @Path("tasks")
    @Produces({ MediaType.APPLICATION_JSON })
    public List<TaskExecutorClientInfo> getAllTaskClients(@QueryParam("domain")String domain){
        if(StringUtils.isEmpty(domain)) {
            return taskDiscovery.getList();
        }
        else{
            return taskDiscovery.getList(domain);
        }
    }

    @GET
    @Path("tasks/{uid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public TaskExecutorClientInfo getTaskClient(@PathParam("uid") String uid){
        return taskDiscovery.get(uid);
    }
}

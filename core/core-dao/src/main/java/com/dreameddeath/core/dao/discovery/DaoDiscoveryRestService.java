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

package com.dreameddeath.core.dao.discovery;

import com.dreameddeath.core.dao.model.discovery.DaoInstanceInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 09/11/2015.
 */
@Path("/daos")
@Api(value = "/daos", description = "List of registered daos")
public class DaoDiscoveryRestService {
    private DaoDiscovery daoDiscovery;

    @Autowired(required=true)
    public void setDaoDiscovery(DaoDiscovery discovery){
        this.daoDiscovery = discovery;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public List<DaoInstanceInfo> getAll(){
        return daoDiscovery.getList();
    }

    @GET
    @Path("{uid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public DaoInstanceInfo get(@PathParam("uid") String uid){
        return daoDiscovery.get(uid);
    }

}

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

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.curator.discovery.ICuratorDiscovery;
import com.dreameddeath.core.curator.model.IRegisterable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 31/10/2015.
 */
public class RestCuratorDiscovery<T extends IRegisterable> {
    @Autowired
    private ICuratorDiscovery<T> discovery;

    public void setDiscovery(ICuratorDiscovery<T> discovery){
        this.discovery=discovery;
    }

    @GET
    @Path("/instances")
    public List<T> getInstances(){
        return discovery.getList();
    }

    @GET
    @Path("/instances/{uid}")
    public T getInstance(@PathParam("uid") String uid){
        return discovery.get(uid);
    }


}

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

package com.dreameddeath.core.service.config;

import com.dreameddeath.core.service.config.model.UpdateKeyResult;
import org.apache.commons.configuration.AbstractConfiguration;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 11/11/2015.
 */
@Path("/")
public class ConfigManagementService {
    private boolean readOnly=false;

    private AbstractConfiguration config;

    @Required
    public void setConfig(AbstractConfiguration config){
        this.config = config;
    }

    public void setReadOnly(boolean readOnly){
        this.readOnly = readOnly;
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    public Map<String,String> getProperties(){
        Map<String,String> result = new TreeMap<>();
        Iterator<String> keysIterator=config.getKeys();
        while(keysIterator.hasNext()){
            String key=keysIterator.next();
            result.put(key,config.getProperty(key).toString());
        }
        return result;
    }


    @PUT
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public List<UpdateKeyResult> setProperties(Map<String,String> properties){
        if(readOnly){
            throw new NotAuthorizedException("The configuration is read only");
        }
        List<UpdateKeyResult> updateKeyResults=new ArrayList<>(properties.keySet().size());
        //Map<String,String> previousValue = new TreeMap<>();
        for(String key:properties.keySet()){
            UpdateKeyResult result=new UpdateKeyResult();
            updateKeyResults.add(result);
            result.setKey(key);
            result.setNewValue(properties.get(key));
            if(config.containsKey(key)){
                result.setOldValue(config.getString(key));
            }
            config.setProperty(key,result.getNewValue());
        }
        return updateKeyResults;
    }


    @GET
    @Path("/{key}")
    @Produces({MediaType.TEXT_PLAIN})
    public String getProperty(@PathParam("key") String key){
        if(config.containsKey(key)){
            return config.getProperty(key).toString();
        }
        else{
            throw new NotFoundException("Cannot find key <"+key+">");
        }
    }

    @PUT
    @Path("/{key}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.APPLICATION_JSON})
    public UpdateKeyResult updateProperty(@PathParam("key") String key,String value){
        if(readOnly){
            throw new NotAuthorizedException("The configuration is read only");
        }
        if(!config.containsKey(key)){
            throw new NotFoundException("The configuration entry <"+key+"> is not existing");

        }
        UpdateKeyResult result = new UpdateKeyResult();
        result.setKey(key);
        result.setOldValue(config.getString(key));
        result.setNewValue(value);
        //String oldValue = config.getProperty(key).toString();
        config.setProperty(key,value);
        return result;
    }


    @POST
    @Path("/{key}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN})
    public String createProperty(@PathParam("key") String key,String value){
        if(readOnly){
            throw new NotAuthorizedException("The configuration is read only");
        }
        if(config.containsKey(key)){
            throw new NotAuthorizedException("The configuration <"+key+"> is already existing ");
        }
        config.addProperty(key,value);
        return value;
    }

    @DELETE
    @Path("/{key}")
    @Produces({MediaType.TEXT_PLAIN})
    public String deleteProperty(@PathParam("key") String key){
        if(readOnly){
            throw new NotAuthorizedException("The configuration is read only");
        }
        if(!config.containsKey(key)){
            throw new NotAuthorizedException("The configuration <"+key+"> isn't existing ");
        }
        String value = config.getString(key);
        config.clearProperty(key);
        return value;
    }


}

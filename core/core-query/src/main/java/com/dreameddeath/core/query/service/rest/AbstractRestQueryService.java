/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.query.service.rest;

import com.dreameddeath.core.query.model.v1.QuerySearch;
import com.dreameddeath.core.query.service.IQueryService;
import com.dreameddeath.core.service.AbstractRestExposableService;
import com.dreameddeath.core.user.IUser;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Created by Christophe Jeunesse on 27/12/2016.
 */
public abstract class AbstractRestQueryService<T> extends AbstractRestExposableService {
    private final Logger LOG = LoggerFactory.getLogger(AbstractRestQueryService.this.getClass());
    public static final String SERVICE_TYPE="query";

    protected abstract IQueryService<T> getQueryService();

    public void doGet(String key, IUser user, AsyncResponse response){
        try {
            getQueryService()
                    .asyncGet(key,user)
                    .doOnError(throwable -> LOG.error("An error occurs while query <"+key+">",throwable))
                    .subscribe(response::resume,response::resume);
        }
        catch (Throwable e){
            LOG.error("An error occurs while query <"+key+">",e);
            response.resume(e);
        }
    }

    public void doSearch(String type, MultivaluedMap<String,String> params, IUser user, AsyncResponse response) {
        Preconditions.checkArgument(type!=null,"The type should be provided");
        final QuerySearch.SearchType searchType=QuerySearch.SearchType.valueOf(type.toUpperCase());
        Preconditions.checkArgument(params!=null,"The params should be provided");
        try{
            getQueryService()
                    .asyncSearch(new QuerySearch(searchType,params), user)
                    .toList()
                    .doOnError(throwable -> LOG.error("Error during quering <"+type+"> with params <"+params+">",throwable))
                    .subscribe(response::resume,response::resume);
        }
        catch(Throwable e){
            LOG.error("Error during quering <"+type+"> with params <"+params+">",e);
            response.resume(e);
        }
    }
}

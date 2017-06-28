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

package com.dreameddeath.core.query.service.remote;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.query.model.v1.QuerySearch;
import com.dreameddeath.core.query.service.IQueryService;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.dreameddeath.core.user.IUser;
import io.reactivex.Observable;
import io.reactivex.Single;

import javax.ws.rs.core.MediaType;

/**
 * Created by christophe jeunesse on 19/06/2017.
 */
public class RemoteQueryService<T> implements IQueryService<T> {
    private IRestServiceClient serviceClient;
    private Class<T> dtoModelClass;

    public void setRestServiceClient(IRestServiceClient client){
        this.serviceClient = client;
    }

    public void setDtoModelClass(Class<T> dtoModelClass){
        this.dtoModelClass = dtoModelClass;
    }


    @Override
    public Single<T> asyncGet(String key, ICouchbaseSession session) {
        return asyncGet(key,session.getUser());
    }

    @Override
    public Observable<T> asyncSearch(QuerySearch search, ICouchbaseSession session) {
        return null;
    }

    @Override
    public Single<T> asyncGet(String key, IUser user) {
        return serviceClient.getInstance()
                .path("/{key}")
                .resolveTemplate("key",key)
                //.queryParam("key",key)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .property(IServiceClient.USER_PROPERTY,user)
                .get(dtoModelClass);
    }

    @Override
    public Observable<T> asyncSearch(QuerySearch search, IUser user) {
        return null;
    }
}

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

package com.dreameddeath.core.helper;

import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.helper.service.AbstractDaoRestService;
import com.dreameddeath.core.helper.service.DaoHelperServiceUtils;
import com.dreameddeath.core.helper.service.SerializableViewQueryRow;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.user.IUser;
import io.swagger.annotations.Api;
import rx.Observable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 14/04/2015.
 */
@Path("testDomain/v1.0/test") //${service.domain}/v${service.version}/${service.name.toLowerCase()}
@ServiceDef(domain="test",name="dao$testDomain$test",version="1.0",status = VersionStatus.STABLE)
@Api(value = "testDomain/v1.0/test", description = "Basic resource")
public class TestDaoRestService extends AbstractDaoRestService {

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getAll(
            @HeaderParam("USER_TOKEN") String userToken,
            @QueryParam("key") String key,
            @QueryParam("keys") Collection<String> keys,
            //start/end key case
            @QueryParam("startKey") String startKey,
            @QueryParam("endKey") String endKey,
            @QueryParam("inclusiveEndKey") Boolean inclusiveEndKey,
            //miscellaneous params
            @QueryParam("descending") Boolean descending,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            //Continuing Case
            @QueryParam("token") String token, @QueryParam("nb") Integer nbMore
    ) throws Exception
    {
        IUser user = getUserFactory().fromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadOnlySession(user);
        IViewQuery<String,String,TestDoc> query  = session.initViewQuery(TestDoc.class,"all_test");

        if(key!=null){ query.withKey(key);}
        else if(keys!=null && (keys.size()>0)){ query.withKeys(keys); }
        else if(startKey!=null){
            if(endKey==null){
                endKey = startKey;
            }
            if(inclusiveEndKey==null){
                inclusiveEndKey = true;
            }
            query.withStartKey(startKey);
            query.withEndKey(endKey,inclusiveEndKey);
        }

        if(descending!=null){
            query.withDescending(descending);
        }
        if(offset!=null){
            query.withOffset(offset);
        }
        if(limit!=null){
            query.withLimit(limit);
        }

        Observable<IViewAsyncQueryResult<String,String,TestDoc>> resultObservable = session.executeAsyncQuery(query);
        ///TODO replace by chuncked result
        IViewAsyncQueryResult<String,String,TestDoc> result=resultObservable.toBlocking().first();
        if(result.getSuccess()){

            return Response.ok(result.getRows().map(SerializableViewQueryRow<String,String,TestDoc>::new).toList().toBlocking().first(),MediaType.APPLICATION_JSON_TYPE)
                    //TODO build token
                    .header(DaoHelperServiceUtils.HTTP_HEADER_QUERY_TOTAL_ROWS, result.getTotalRows())
                    .build();
        }
        else{
            return Response.serverError().entity(result.getErrorInfo().toBlocking().first()).type(MediaType.APPLICATION_JSON_TYPE).build();///TODO retrieve error info
        }
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response create(@HeaderParam("USER_TOKEN") String userToken, @HeaderParam("DOC_FLAGS")Integer flags,TestDoc documentToCreate) throws Exception{
        IUser user = getUserFactory().fromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadWriteSession(user);
        session.attachEntity(documentToCreate);
        if(flags!=null){
            documentToCreate.getBaseMeta().setEncodedFlags(flags);
        }
        session.toBlocking().create(documentToCreate);
        return Response.ok(documentToCreate, MediaType.APPLICATION_JSON_TYPE)
                .build();
    }


    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("{id}")
    public Response read(@HeaderParam("USER_TOKEN") String userToken,
                         @PathParam("id") String id) throws Exception{
        IUser user = getUserFactory().fromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadOnlySession(user);
        TestDoc doc = session.toBlocking().get(String.format("test/%s",id),TestDoc.class);
        return Response.ok(doc,MediaType.APPLICATION_JSON_TYPE)
                .build();
    }


    @DELETE
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("{id}")
    public Response delete(@HeaderParam("USER_TOKEN") String userToken,
                         @PathParam("id") String id) throws Exception{
        IUser user = getUserFactory().fromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadOnlySession(user);
        TestDoc doc = session.toBlocking().get(String.format("test/%s",id),TestDoc.class);
        session.toBlocking().delete(doc);
        return Response.ok(doc,MediaType.APPLICATION_JSON_TYPE)
                .build();
    }


    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("{id}")
    public Response replace(@HeaderParam("USER_TOKEN") String userToken,
                           @HeaderParam(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV) Long casData,
                           @HeaderParam(DaoHelperServiceUtils.HTTP_HEADER_DOC_FLAGS) Integer flags,
                           @PathParam("id") String id,
                           TestDoc updatedDocument) throws Exception{
        IUser user = getUserFactory().fromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadWriteSession(user);
        updatedDocument.getBaseMeta().setKey(String.format("test/%s", id));
        updatedDocument.getBaseMeta().setCas(casData);
        if(flags!=null) {
            updatedDocument.getBaseMeta().setEncodedFlags(flags);
        }
        updatedDocument.getBaseMeta().setStateSync();
        updatedDocument.getBaseMeta().setStateDirty();
        session.attachEntity(updatedDocument);
        session.toBlocking().update(updatedDocument);
        return Response.ok(updatedDocument,MediaType.APPLICATION_JSON_TYPE)
                .build();
    }


    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("_queries/testview")
    public Response getFromViewTestView(
                @HeaderParam("USER_TOKEN") String userToken,
                @QueryParam("key") String key,
                @QueryParam("keys") Collection<String> keys,
                //start/end key case
                @QueryParam("startKey") String startKey,
                @QueryParam("endKey") String endKey,
                @QueryParam("inclusiveEndKey") Boolean inclusiveEndKey,
                //miscellaneous params
                @QueryParam("descending") Boolean descending,
                @QueryParam("offset") Integer offset,
                @QueryParam("limit") Integer limit,
                //Continuing Case
                @QueryParam("token") String token, @QueryParam("nb") Integer nbMore
        ) throws Exception
    {
        IUser user = getUserFactory().fromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadOnlySession(user);
        IViewQuery<String,String,TestDoc> query  = session.initViewQuery(TestDoc.class,"testView");

        if(key!=null){ query.withKey(key);}
        else if(keys!=null && (keys.size()>0)){ query.withKeys(keys); }
        else if(startKey!=null){
            if(endKey==null){
                endKey = startKey;
            }
            if(inclusiveEndKey==null){
                inclusiveEndKey = true;
            }
            query.withStartKey(startKey);
            query.withEndKey(endKey,inclusiveEndKey);
        }

        if(descending!=null){
            query.withDescending(descending);
        }
        if(offset!=null){
            query.withOffset(offset);
        }
        if(limit!=null){
            query.withLimit(limit);
        }

        Observable<IViewAsyncQueryResult<String,String,TestDoc>> resultObservable = session.executeAsyncQuery(query);
        ///TODO replace by chuncked result
        IViewAsyncQueryResult<String,String,TestDoc> result=resultObservable.toBlocking().first();
        if(result.getSuccess()){
            return Response.ok(result.getRows().map(SerializableViewQueryRow<String,String,TestDoc>::new).toList().toBlocking().first(),MediaType.APPLICATION_JSON_TYPE)
                    //TODO build token
                    .header(DaoHelperServiceUtils.HTTP_HEADER_QUERY_TOTAL_ROWS, result.getTotalRows())
                    .build();
        }
        else{
            return Response.serverError().entity(result.getErrorInfo().toBlocking().first()).type(MediaType.APPLICATION_JSON_TYPE).build();///TODO retrieve error info
        }
    }

}

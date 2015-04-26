/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao;

import com.dreameddeath.core.dao.helper.service.AbstractDaoRestService;
import com.dreameddeath.core.dao.helper.service.DaoHelperServiceUtils;
import com.dreameddeath.core.dao.helper.service.SerializableViewQueryRow;
import com.dreameddeath.core.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.model.view.IViewQuery;
import com.dreameddeath.core.model.view.IViewQueryRow;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.user.IUser;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import rx.Observable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by CEAJ8230 on 14/04/2015.
 */
@Path("testDomain/v1.0/test/{testDocId}/child") //${service.domain}/v${service.version}/${service.name.toLowerCase()}
@ServiceDef(name="dao$testDomain$testChild",version="1.0",status = VersionStatus.STABLE)
@Api(value = "testDomain/v1.0/test/{testDocId}/child", description = "Basic Sub resource")
public class TestChildDaoRestService extends AbstractDaoRestService {


    public static class GetAllViewResult extends SerializableViewQueryRow<String,String,TestDocChild>{
        public GetAllViewResult(){super();}
        public GetAllViewResult(IViewQueryRow<String,String,TestDocChild> list){super(list);}
    }
    /*@ApiModel
    public interface AllListResponse implements List<SerializableViewQueryRow<String,String,TestDocChild>> {}*/
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(
            value = "Lookup for all elements",
            response = GetAllViewResult.class,
            responseContainer = "List",
            position = 0)
    /*
    TODO Map API response
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 404, message = "object not found")
    })*/
    public Response getAll(
            @HeaderParam("USER_TOKEN") String userToken,
            @ApiParam("the parent test doc key") @PathParam("testDocId") String testDocId,
            @ApiParam("[EXACT SEARCH]the exact key to look for") @QueryParam("key") String key,
            @ApiParam("[LIST SEARCH]The list of key to search for") @QueryParam("keys") List<String> keys,
            //start/end key case
            @ApiParam("[RANGE SEARCH]The start key to search for") @QueryParam("startKey") String startKey,
            @ApiParam("[RANGE SEARCH]The end key to search for")  @QueryParam("endKey") String endKey,
            @ApiParam(value = "[RANGE SEARCH]flag to tell if end key lookup is inclusive",defaultValue = "false")  @QueryParam("inclusiveEndKey") Boolean inclusiveEndKey,
            //miscellaneous params
            @ApiParam(value = "Sort in descending keys",defaultValue = "false") @QueryParam("descending") Boolean descending,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            //Continuing Case
            @QueryParam("token") String token, @QueryParam("nb") Integer nbMore
    ) throws Exception
    {
        IUser user = getUserFactory().validateFromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadOnlySession(user);
        IViewQuery<String,String,TestDocChild> query  = session.initViewQuery(TestDocChild.class,"all_testChild");

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
        else{
            query.withKey(String.format("test/%s",testDocId));
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

        Observable<IViewAsyncQueryResult<String,String,TestDocChild>> resultObservable = session.executeAsyncQuery(query);
        ///TODO replace by chuncked result
        IViewAsyncQueryResult<String,String,TestDocChild> result=resultObservable.toBlocking().first();
        if(result.getSuccess()){

            return Response.ok(result.getRows().map(SerializableViewQueryRow<String,String,TestDocChild>::new).toList().toBlocking().first(),MediaType.APPLICATION_JSON_TYPE)
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
    @ApiOperation(
            value = "Creates a new Child elment",
            response = TestDocChild.class,
            position = 1)
    public Response create(@HeaderParam("USER_TOKEN") String userToken,
                           @HeaderParam("DOC_FLAGS")Integer flags,
                           @PathParam("testDocId") String testDocId,
                           TestDocChild documentToCreate) throws Exception{
        IUser user = getUserFactory().validateFromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadWriteSession(user);
        session.attachEntity(documentToCreate);
        documentToCreate.parent= new TestDocLink();
        documentToCreate.parent.setKey(String.format("test/%s",testDocId));
        if(flags!=null){
            documentToCreate.getBaseMeta().setEncodedFlags(flags);
        }
        //session.save(documentToCreate);
        session.create(documentToCreate);
        return Response.ok(documentToCreate, MediaType.APPLICATION_JSON_TYPE)
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY, documentToCreate.getBaseMeta().getKey())
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV, Long.toString(documentToCreate.getBaseMeta().getCas()))
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_FLAGS, Long.toString(documentToCreate.getBaseMeta().getEncodedFlags()))
                .build();
    }


    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("{id}")
    public Response read(@HeaderParam("USER_TOKEN") String userToken,
                         @PathParam("testDocId") String testDocId,
                         @PathParam("id") String id) throws Exception{
        IUser user = getUserFactory().validateFromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadOnlySession(user);
        TestDocChild doc = session.get(String.format("test/%s/child/%s", testDocId,id),TestDocChild.class);
        return Response.ok(doc,MediaType.APPLICATION_JSON_TYPE)
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY, doc.getBaseMeta().getKey())
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV, Long.toString(doc.getBaseMeta().getCas()))
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_FLAGS, Long.toString(doc.getBaseMeta().getEncodedFlags()))
                .build();
    }


    @DELETE
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("{id}")
    public Response delete(@HeaderParam("USER_TOKEN") String userToken,
                           @PathParam("testDocId") String testDocId,
                           @PathParam("id") String id) throws Exception{
        IUser user = getUserFactory().validateFromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadOnlySession(user);
        TestDocChild doc = session.get(String.format("test/%s/child/%s", testDocId,id),TestDocChild.class);
        session.delete(doc);
        return Response.ok(doc,MediaType.APPLICATION_JSON_TYPE)
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY, doc.getBaseMeta().getKey())
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV, Long.toString(doc.getBaseMeta().getCas()))
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_FLAGS, Long.toString(doc.getBaseMeta().getEncodedFlags()))
                .build();
    }


    @PUT
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("{id}")
    public Response replace(@HeaderParam("USER_TOKEN") String userToken,
                            @HeaderParam(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV) Long casData,
                            @HeaderParam(DaoHelperServiceUtils.HTTP_HEADER_DOC_FLAGS) Integer flags,
                            @PathParam("testDocId") String testDocId,
                            @PathParam("id") String id,
                            TestDocChild updatedDocument) throws Exception{
        IUser user = getUserFactory().validateFromToken(userToken);
        ICouchbaseSession session = getSessionFactory().newReadWriteSession(user);
        updatedDocument.getBaseMeta().setKey(String.format("test/%s/child/%s", testDocId,id));
        updatedDocument.getBaseMeta().setCas(casData);
        if(flags!=null) {
            updatedDocument.getBaseMeta().setEncodedFlags(flags);
        }
        updatedDocument.getBaseMeta().setStateSync();
        updatedDocument.getBaseMeta().setStateDirty();
        session.attachEntity(updatedDocument);
        session.update(updatedDocument);
        return Response.ok(updatedDocument,MediaType.APPLICATION_JSON_TYPE)
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_KEY, updatedDocument.getBaseMeta().getKey())
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_REV, Long.toString(updatedDocument.getBaseMeta().getCas()))
                .header(DaoHelperServiceUtils.HTTP_HEADER_DOC_FLAGS, Long.toString(updatedDocument.getBaseMeta().getEncodedFlags()))
                .build();
    }



}

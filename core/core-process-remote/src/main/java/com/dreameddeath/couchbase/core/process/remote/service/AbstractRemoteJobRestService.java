/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.couchbase.core.process.remote.service;

import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.process.exception.DuplicateJobExecutionException;
import com.dreameddeath.core.process.exception.JobObservableExecutionException;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.IJobExecutorClientFactory;
import com.dreameddeath.core.process.utils.ProcessUtils;
import com.dreameddeath.core.service.AbstractRestExposableService;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.couchbase.core.process.remote.model.rest.ActionRequest;
import com.dreameddeath.couchbase.core.process.remote.model.rest.AlreadyExistingJob;
import com.dreameddeath.couchbase.core.process.remote.model.rest.RemoteJobResultWrapper;
import com.dreameddeath.couchbase.core.process.remote.model.rest.StateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Christophe Jeunesse on 15/01/2016.
 */
public abstract class AbstractRemoteJobRestService<TJOB extends AbstractJob,TREQ,TRESP> extends AbstractRestExposableService {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractRemoteJobRestService.class);
    public static final String REQUEST_UID_QUERY_PARAM = "requestUid";
    public static final String SUBMIT_ONLY_QUERY_PARAM = "submitOnly";
    private final Class<TJOB> jobClass;
    private final Constructor<? extends RemoteJobResultWrapper<TRESP>> jobResultWrapperConstructor;
    private ICouchbaseSessionFactory sessionFactory;
    private IJobExecutorClient<TJOB> jobExecutorClient;

    @Autowired
    public void setJobExecutorClientFactory(IJobExecutorClientFactory jobExecutorClientFactory) {
        this.jobExecutorClient = jobExecutorClientFactory.buildJobClient(jobClass);
    }

    @Autowired
    public void setSessionFactory(ICouchbaseSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected abstract TJOB buildJobFromRequest(TREQ request);
    protected abstract TRESP buildResponse(TJOB response);
    protected abstract Class<? extends RemoteJobResultWrapper<TRESP>> getResponseClass();

    @SuppressWarnings("unchecked")
    public AbstractRemoteJobRestService(){
        //
        jobClass = ClassUtils.getEffectiveGenericType(this.getClass(),AbstractRemoteJobRestService.class,0);
        Class responseClass = ClassUtils.getEffectiveGenericType(this.getClass(),AbstractRemoteJobRestService.class,2);
        if(responseClass==null){
            throw new RuntimeException("Cannot find response class "+this.getClass().getName());
        }
        Class<?> jaxrsReponseClass=getResponseClass();
        Constructor<? extends RemoteJobResultWrapper<TRESP>> foundConstructor = null;
        for(Constructor constructor:jaxrsReponseClass.getConstructors()){
            if((constructor.getParameters().length==1) && responseClass.isAssignableFrom(constructor.getParameters()[0].getType())){
                foundConstructor = (Constructor<RemoteJobResultWrapper<TRESP>>)constructor;
                break;
            }
        }
        jobResultWrapperConstructor = foundConstructor;
        if(jobResultWrapperConstructor==null){
            throw new RuntimeException("Cannot find constructor of class "+jaxrsReponseClass.getName()+" with parameter of class <"+responseClass.getName()+">");
        }
    }


    private Response buildJaxrsResponse(TJOB job){
        try {
            RemoteJobResultWrapper<TRESP> response = jobResultWrapperConstructor.newInstance(buildResponse(job));
            response.setJodId(job.getUid());
            response.setJobStateInfo(new StateInfo(job.getStateInfo()));
            return Response.ok().entity(new GenericEntity<>(response, getResponseClass())).build();
        }
        catch(InstantiationException|IllegalAccessException|InvocationTargetException e){
            throw new RuntimeException(e);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void runJobCreate(@Context IUser user,
                                 @QueryParam(SUBMIT_ONLY_QUERY_PARAM) Boolean submitOnly,
                                 @QueryParam(REQUEST_UID_QUERY_PARAM) String requestUid,
                                 TREQ request,
                                 @Suspended final AsyncResponse asyncResponse){
        try {
            TJOB job = buildJobFromRequest(request);
            if(StringUtils.isNotEmpty(requestUid)){
                job.setRequestUid(requestUid);
            }
            Observable<JobContext<TJOB>> jobCtxtObs;
            if (submitOnly!=null && submitOnly) {
                jobCtxtObs= jobExecutorClient.submitJob(job, user);
            }
            else {
                jobCtxtObs = jobExecutorClient.executeJob(job, user);
            }
            jobCtxtObs
                    .map(ctxt->buildJaxrsResponse(ctxt.getInternalJob()))
                    .onErrorResumeNext(this::manageStandardErrors)
                    .subscribe(asyncResponse::resume,(throwable -> {
                        try {
                            if(throwable instanceof DuplicateJobExecutionException) {
                                DuplicateJobExecutionException e = (DuplicateJobExecutionException) throwable;
                                AlreadyExistingJob result = new AlreadyExistingJob();
                                result.key = e.getOwnerDocumentKey();
                                try {
                                    ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY, user);
                                    AbstractJob conflictingJob = session.toBlocking().blockingGet(e.getOwnerDocumentKey(), AbstractJob.class);
                                    result.requestUid = conflictingJob.getRequestUid();
                                    result.uid = conflictingJob.getUid().toString();
                                    result.jobModelId = conflictingJob.getModelId().toString();
                                } catch (Throwable lookupError) {
                                    //ignore error
                                }
                                asyncResponse.resume(Response.status(Response.Status.CONFLICT).entity(result).build());
                            } else {
                                LOG.error("An error occurs while executing job <"+(requestUid!=null?requestUid:"null")+"> of with service <"+AbstractRemoteJobRestService.this.getClass().getName()+">",throwable);
                                asyncResponse.resume(throwable);
                            }
                        }
                        catch(Throwable e){
                            LOG.error("An error occurs while executing job <"+requestUid+"> of with service <"+AbstractRemoteJobRestService.this.getClass().getName()+">",throwable);
                            asyncResponse.resume(e);
                        }
                    }));
        }
        catch (Throwable e){
            asyncResponse.resume(e);
        }
    }

    @GET
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getJob(@Context IUser user,
                           @PathParam("uid") String uid,
                           @Suspended AsyncResponse asyncResponse) {
        try {
            ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY, user);
            ProcessUtils.asyncLoadJob(session, uid, jobClass)
                    .map(this::buildJaxrsResponse)
                    .onErrorResumeNext(this::manageStandardErrors)
                    .doOnError(throwable -> LOG.error("An error occurs while executing job <"+uid+"> of with service <"+AbstractRemoteJobRestService.this.getClass().getName()+">",throwable))
                    .subscribe(asyncResponse::resume, asyncResponse::resume)
            ;
        }
        catch (Throwable e){
            asyncResponse.resume(e);
        }
    }

    @PUT
    @Path("/{uid}/{action:cancel|resume}")
    @Produces(MediaType.APPLICATION_JSON)
    public void updateJob(@Context final IUser user,
                                @PathParam("uid") final String uid,
                                @QueryParam(REQUEST_UID_QUERY_PARAM) final String requestUid,
                                @PathParam("action") final ActionRequest actionRequest,
                                @Suspended final AsyncResponse asyncResponse){
        if(actionRequest==null){
            asyncResponse.resume(new BadRequestException("The action is inconsistent"));
        }
        try {
            ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY, user);
            ProcessUtils.asyncLoadJob(session, uid, jobClass)
                    .flatMap(job->this.checkRequestId(requestUid,job))
                    .flatMap(job->{
                        switch (actionRequest) {
                            case RESUME:
                                return jobExecutorClient.resumeJob(job, user);
                            case CANCEL:
                                return jobExecutorClient.cancelJob(job, user);
                            default:
                                return Observable.error(new NotSupportedException("Not managed action :"+actionRequest+" on job "+uid));
                        }
                    })
                    .map(ctxt->this.buildJaxrsResponse(ctxt.getInternalJob()))
                    .onErrorResumeNext(this::manageStandardErrors)
                    .doOnError(throwable -> LOG.error("An error occurs while executing job <"+requestUid+"> of with service <"+AbstractRemoteJobRestService.this.getClass().getName()+">",throwable))
                    .subscribe(asyncResponse::resume,asyncResponse::resume);
        }
        catch(Throwable e){
            LOG.error("An error occurs while executing job <"+requestUid+"> of with service <"+AbstractRemoteJobRestService.this.getClass().getName()+">",e);
            asyncResponse.resume(new InternalServerErrorException(e));
        }
    }

    private Observable<Response> manageStandardErrors(Throwable throwable) {
        if(throwable instanceof StorageObservableException && ((StorageObservableException)throwable).isDocumentNotFoundException()){
            DocumentNotFoundException e = (DocumentNotFoundException)((StorageObservableException)throwable).getCause();
            return Observable.error(new NotFoundException("The job "+e.getKey()+ " isn't existing"));
        }
        else if(throwable instanceof JobObservableExecutionException && throwable.getCause()!=null){
            return Observable.error(throwable.getCause());
        }
        else{
            return Observable.error(throwable);
        }
    }

    private Observable<TJOB> checkRequestId(final String requestUid, final TJOB job) {
        if(StringUtils.isNotEmpty(requestUid)){
            if(!requestUid.equals(job.getRequestUid())){
                return Observable.error(new NotFoundException("The job "+job.getBaseMeta().getKey()+ " hasn't right request id <"+requestUid+"> but <"+job.getRequestUid()));
            }
        }
        return Observable.just(job);
    }
}

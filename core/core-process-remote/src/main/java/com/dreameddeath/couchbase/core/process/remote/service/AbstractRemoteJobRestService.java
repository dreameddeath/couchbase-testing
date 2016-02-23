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

package com.dreameddeath.couchbase.core.process.remote.service;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DocumentNotFoundException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.process.exception.DuplicateJobExecutionException;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.factory.IJobExecutorClientFactory;
import com.dreameddeath.core.process.utils.ProcessUtils;
import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.couchbase.core.process.remote.model.rest.ActionRequest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;

/**
 * Created by Christophe Jeunesse on 15/01/2016.
 */
public abstract class AbstractRemoteJobRestService<TJOB extends AbstractJob,TREQ,TRESP> extends AbstractExposableService {
    ICouchbaseSessionFactory sessionFactory;
    IJobExecutorClient<TJOB> jobExecutorClient;

    @Autowired
    public void setJobExecutorClientFactory(IJobExecutorClientFactory jobExecutorClientFactory) {
        this.jobExecutorClient = jobExecutorClientFactory.buildJobClient(getJobClass());
    }

    @Autowired
    public void setSessionFactory(ICouchbaseSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected abstract TJOB buildJobFromRequest(TREQ request);

    protected abstract TRESP buildResponse(TJOB response);

    protected abstract Class<TJOB> getJobClass();


    @POST
    public TRESP runJobCreate(@Context IUser user,
                              @QueryParam("submitOnly") Boolean submitOnly,
                              TREQ request) {
        try {
            JobContext<TJOB> result;
            TJOB job = buildJobFromRequest(request);
            if (submitOnly) {
                result = jobExecutorClient.submitJob(job, user);
            }
            else {
                result = jobExecutorClient.executeJob(job, user);
            }
            return buildResponse(result.getJob());
        } catch (DuplicateJobExecutionException e) {
            throw new NotAllowedException("The job " + e.getKey() + " is already existing with job key <" + e.getOwnerDocumentKey() + ">", e, "PUT", "GET");
        } catch (JobExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/{uid}")
    public TRESP getJobCreate(@Context IUser user,
                              @PathParam("uid") String uid) {
        ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY, user);
        try {
            TJOB createJob = ProcessUtils.loadJob(session, uid, getJobClass());
            return buildResponse(createJob);
        } catch (DocumentNotFoundException e) {
            throw new NotFoundException(e);
        } catch (StorageException | DaoException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @PUT
    @Path("/{uid}/{action:cancel|resume}")
    public TRESP updateJobCreate(@Context IUser user,
                                 @PathParam("uid")String uid,
                                 @PathParam("action") ActionRequest actionRequest){
        if(actionRequest==null){
            throw new BadRequestException("The action is iconsistent");
        }
        try {
            ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY, user);
            TJOB createJob = ProcessUtils.loadJob(session, uid, getJobClass());
            JobContext<TJOB> result;
            switch (actionRequest) {
                case RESUME:
                    result = jobExecutorClient.resumeJob(createJob, user);
                    break;
                case CANCEL:
                    result = jobExecutorClient.cancelJob(createJob, user);
                    break;
                default:
                    throw new NotSupportedException("Not managed action :"+actionRequest+" on job "+uid);
            }
            return buildResponse(result.getJob());
        }
        catch(StorageException|DaoException|JobExecutionException e){
            throw new InternalServerErrorException(e);
        }
    }


}

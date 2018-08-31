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

package com.dreameddeath.billing.installedbase.process;

import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBase;
import com.dreameddeath.billing.installedbase.process.model.v1.CreateUpdateBillingInstalledBaseJob;
import com.dreameddeath.billing.installedbase.service.ICreateUpdateBillingInstalledBaseService;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseResult;
import com.dreameddeath.billing.model.v1.account.BillingAccount;
import com.dreameddeath.billing.model.v1.account.BillingAccountContributor;
import com.dreameddeath.billing.model.v1.account.BillingAccountContributorLink;
import com.dreameddeath.billing.model.v1.account.BillingAccountLink;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.process.annotation.JobProcessingForClass;
import com.dreameddeath.core.process.annotation.TaskProcessingForClass;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.JobProcessingResult;
import com.dreameddeath.core.process.service.context.TaskContext;
import com.dreameddeath.core.process.service.impl.processor.ChildDocumentCreateOrUpdateTaskProcessingService;
import com.dreameddeath.core.process.service.impl.processor.StandardJobProcessingService;
import com.dreameddeath.core.query.factory.QueryServiceFactory;
import com.dreameddeath.core.query.service.IQueryService;
import com.dreameddeath.installedbase.model.v1.published.query.InstalledBaseResponse;
import com.google.common.base.Preconditions;
import io.reactivex.Single;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by Christophe Jeunesse on 16/11/2016.
 */
@JobProcessingForClass(CreateUpdateBillingInstalledBaseJob.class)
public class CreateUpdateBillingInstalledBaseProcessingService extends StandardJobProcessingService<CreateUpdateBillingInstalledBaseJob> {
    @Override
    public Single<JobProcessingResult<CreateUpdateBillingInstalledBaseJob>> init(JobContext<CreateUpdateBillingInstalledBaseJob> context) {
        Preconditions.checkNotNull(context.getInternalJob().getInstalledBaseKey(),"The installed base Key must be provided");

        //Step one : Create "BillingInstalledBase" or update child document
        //Step two : If created, add it to parent

        context.addTask(new CreateUpdateBillingInstalledBaseJob.CreateUpdateBillingInstalledBaseTask());
        return null;
    }

    @TaskProcessingForClass(CreateUpdateBillingInstalledBaseJob.CreateUpdateBillingInstalledBaseTask.class)
    public static class CreateUpdateBillingInstalledBaseTaskProcessingService extends ChildDocumentCreateOrUpdateTaskProcessingService<CreateUpdateBillingInstalledBaseJob,BillingInstalledBase,BillingAccount,CreateUpdateBillingInstalledBaseJob.CreateUpdateBillingInstalledBaseTask>{
        private ICreateUpdateBillingInstalledBaseService createUpdateBillingInstalledBaseService;
        private QueryServiceFactory queryServiceFactory;
        private IQueryService<InstalledBaseResponse> queryService;

        @Inject
        public void setCreateUpdateBillingInstalledBaseService(ICreateUpdateBillingInstalledBaseService service){
            this.createUpdateBillingInstalledBaseService = service;
        }

        @Inject
        public void queryServiceFactory(QueryServiceFactory queryServiceFactory){
            this.queryServiceFactory = queryServiceFactory;
        }

        @PostConstruct
        public void init(){
            queryService = this.queryServiceFactory.getQueryService(InstalledBaseResponse.class);
        }

        @Override
        protected boolean needParentUpdate(BillingAccount parent, BillingInstalledBase child) {
            return parent.getContributors().stream().anyMatch(contrib->contrib.getKey().equals(child.getBaseMeta().getKey()));
        }

        @Override
        protected void updateParent(BillingAccount parent, BillingInstalledBase child) {
            BillingAccountContributorLink billingAccountContributorLink = new BillingAccountContributorLink(child);
            billingAccountContributorLink.setSourceKey(child.getInstalledBaseKey());
            billingAccountContributorLink.setType(BillingAccountContributor.ContributorType.RECURRING);
            parent.addContributors(billingAccountContributorLink);
        }

        @Override
        protected Single<FindAndGetResult> findAndGetExistingDocument(TaskContext<CreateUpdateBillingInstalledBaseJob, CreateUpdateBillingInstalledBaseJob.CreateUpdateBillingInstalledBaseTask> taskContext) {
            try {
                return taskContext.getSession()
                        .executeAsyncQuery(
                                taskContext.getSession().
                                        initViewQuery(BillingInstalledBase.class, "billingInstalledBaseSearch")
                                        .withKeys(Arrays.asList(
                                                taskContext.getInternalTask().getParentDocKey(),
                                                taskContext.getParentInternalJob().getInstalledBaseKey())
                                        )
                        )
                        .flatMapObservable(IViewAsyncQueryResult::getRows)
                        .flatMapSingle(row->row.getDoc(taskContext.getSession()))
                        .map(doc->new FindAndGetResult(taskContext,doc))
                        .single(new FindAndGetResult(taskContext));
            }
            catch(DaoException e){
                return Single.error(e);
            }
        }

        @Override
        protected Single<ContextAndDocument> initEmptyDocument(TaskContext<CreateUpdateBillingInstalledBaseJob, CreateUpdateBillingInstalledBaseJob.CreateUpdateBillingInstalledBaseTask> taskContext) {
            BillingInstalledBase billingInstalledBase = new BillingInstalledBase();
            billingInstalledBase.setInstalledBaseKey(taskContext.getParentInternalJob().getInstalledBaseKey());
            return taskContext.getSession().asyncGet(taskContext.getInternalTask().getParentDocKey(),BillingAccount.class)
            .flatMap(baDoc->{
                billingInstalledBase.setBaLink(new BillingAccountLink(baDoc));
                return buildContextAndDocumentObservable(taskContext,billingInstalledBase);
            });
        }

        @Override
        protected Single<DuplicateUniqueKeyCheckResult> onDuplicateUniqueKey(ContextAndDocument ctxt, DuplicateUniqueKeyDaoException e) {
            Preconditions.checkArgument(e.getDoc()!=null && e.getDoc() instanceof BillingInstalledBase);
            BillingInstalledBase doc = (BillingInstalledBase) e.getDoc();
            Preconditions.checkArgument(doc.getInstalledBaseKey().equals(ctxt.getCtxt().getParentInternalJob().getInstalledBaseKey()));
            Preconditions.checkArgument(doc.getBaLink().getKey().equals(ctxt.getCtxt().getInternalTask().getParentDocKey()));
            return Single.just(new DuplicateUniqueKeyCheckResult(ctxt.getCtxt(), doc));
        }

        @Override
        protected Single<ProcessingDocumentResult> processDocument(ContextAndDocument ctxtAndDoc) {
            String installedBaseKey = ctxtAndDoc.getCtxt().getParentInternalJob().getInstalledBaseKey();
            return queryService
                    .asyncGet(installedBaseKey,ctxtAndDoc.getCtxt().getSession())
                    .map(installedBaseResponse -> {
                        CreateUpdateBillingInstalledBaseResult updateBillingInstalledBaseResult = createUpdateBillingInstalledBaseService.createUpdateBillingInstalledBase(installedBaseResponse, ctxtAndDoc.getDoc());
                        ctxtAndDoc.getCtxt().getInternalTask().setResult(updateBillingInstalledBaseResult);
                        return new ProcessingDocumentResult(true,ctxtAndDoc);
                    })

            ;
        }
    }
}

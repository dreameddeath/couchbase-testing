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

package com.dreameddeath.billing.installedbase.notification.listener;

import com.dreameddeath.billing.installedbase.process.model.v1.CreateUpdateBillingInstalledBaseJob;
import com.dreameddeath.billing.model.EntityConstants;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.annotation.ListenerProcessor;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalStandardListener;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.factory.IJobExecutorClientFactory;
import com.dreameddeath.core.process.utils.ProcessUtils;
import com.dreameddeath.installedbase.model.notifications.v1.published.notification.CreateUpdateInstalledBaseEvent;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by christophe jeunesse on 23/06/2017.
 */
@Listener
public class InstalledBaseCreateUpdateListener extends AbstractLocalStandardListener {
    private IJobExecutorClient<CreateUpdateBillingInstalledBaseJob> processingService;

    @Autowired
    public void setExecutorClientFactory(IJobExecutorClientFactory factory){
        processingService = factory.buildJobClient(CreateUpdateBillingInstalledBaseJob.class);
    }

    @Override
    public String getDomain() {
        return EntityConstants.BILLING_DOMAIN;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @ListenerProcessor
    public Single<AbstractNotificationProcessor.ProcessingResult> processInstalledBaseEvent(CreateUpdateInstalledBaseEvent event, ICouchbaseSession session){
        CreateUpdateBillingInstalledBaseJob job = new CreateUpdateBillingInstalledBaseJob();
        job.setInstalledBaseKey(event.getInstalledBaseKey());
        ProcessUtils.setJobRequestId(job,event,getName());
        return processingService.executeJob(job, session)
                .flatMap(ProcessUtils::mapJobResultToNotificationProcessingResult);
    }

}

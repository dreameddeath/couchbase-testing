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

package com.dreameddeath.core.notification.remote;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractDiscoverableListener;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.remote.model.RemoteProcessingResult;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Entity;

/**
 * Created by Christophe Jeunesse on 05/10/2016.
 */
@Listener(forTypes = {AbstractRemoteConsumerRest.REMOTE_NOTIFICATION_LISTENER_TYPE},matcherRank = 10)
public class RemoteProducerListener extends AbstractDiscoverableListener {
    private IRemoteNotificationClientServiceFactory factory;
    private IRestServiceClient remoteClient;

    public RemoteProducerListener(ListenerDescription description) {
        super(description);
    }

    @Autowired
    public void setClientFactory(IRemoteNotificationClientServiceFactory factory){
        this.factory = factory;
    }

    @PostConstruct
    public void init(){
        remoteClient = factory.getClient(getDescription());
    }

    @Override
    protected void incrementAttemptsManagement(Notification sourceNotif) {
        sourceNotif.incNbRemoteAttempts();
    }

    @Override
    protected <T extends IEvent> Single<ProcessingResultInfo> doProcess(T event, Notification notification, ICouchbaseSession session) {
        return session.asyncSave(notification)
                .flatMap(savedNotif->
                    remoteClient.getInstance()
                    .path("{domain}/{id}")
                    .resolveTemplate("domain",notification.getDomain())
                    .resolveTemplate("id",notification.getBaseMeta().getKey())
                    .request()
                    .post(Entity.json(""), RemoteProcessingResult.class)
                    .flatMap(remoteProcessingResult -> mapResult(remoteProcessingResult,session))
                )
                .onErrorReturn(throwable -> ProcessingResultInfo.build(notification,false,ProcessingResult.DEFERRED))
                ;
    }

    public Single<ProcessingResultInfo> mapResult(final RemoteProcessingResult result, ICouchbaseSession session){
        return session.asyncGet(result.getNotificationKey(),Notification.class)
                .map(notif->ProcessingResultInfo.build(notif,true,result.isSuccess()?ProcessingResult.PROCESSED:ProcessingResult.DEFERRED));
    }
}

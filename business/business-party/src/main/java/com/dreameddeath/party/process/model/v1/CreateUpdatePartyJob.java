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

package com.dreameddeath.party.process.model.v1;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateTask;
import com.dreameddeath.couchbase.core.process.remote.annotation.FieldFilteringMode;
import com.dreameddeath.couchbase.core.process.remote.annotation.Request;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;
import com.dreameddeath.couchbase.core.process.remote.annotation.Result;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.process.model.v1.party.CreateUpdatePartyRequest;
import com.dreameddeath.party.service.model.PartyUpdateResult;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DocumentEntity @RestExpose(rootPath = "partyjobs/createupdate",domain = "party",name = "createupdatepartyjob")
public class CreateUpdatePartyJob extends AbstractJob {
    /**
     *  request : The create/update request
     */
    @DocumentProperty("request") @Request(unwrap=true,mode=FieldFilteringMode.FULL)
    private Property<CreateUpdatePartyRequest> request = new StandardProperty<>(CreateUpdatePartyJob.this);

    /**
     *  response : the result of the create/update
     */
    @DocumentProperty("response") @Result(unwrap = true,mode= FieldFilteringMode.FULL)
    private Property<PartyUpdateResult> response = new StandardProperty<>(CreateUpdatePartyJob.this);

    /**
     * Getter of request
     * @return the value of request
     */
    public CreateUpdatePartyRequest getRequest() { return request.get(); }
    /**
     * Setter of request
     * @param val the new value of request
     */
    public void setRequest(CreateUpdatePartyRequest val) { request.set(val); }

    /**
     * Getter of response
     * @return the value of response
     */
    public PartyUpdateResult getResponse() { return response.get(); }
    /**
     * Setter of response
     * @param val the new value of response
     */
    public void setResponse(PartyUpdateResult val) { response.set(val); }


    @DocumentEntity
    public static class CreatePartyTask extends DocumentCreateTask<Party> {
    }
}

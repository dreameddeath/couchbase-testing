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

package com.dreameddeath.billing.model.process;

import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.party.model.base.PartyLink;

/**
 * Created by Christophe Jeunesse on 13/08/2014.
 */
public class CreateBillingAccountResult extends BaseCouchbaseDocumentElement {
    @DocumentProperty("baCreated")
    public BillingAccountLink baLink;
    @DocumentProperty("partyLink")
    public PartyLink partyLink;

}

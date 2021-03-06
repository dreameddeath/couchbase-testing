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

package com.dreameddeath.billing.installedbase.service;

import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBaseItem;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseAction;
import com.dreameddeath.billing.installedbase.service.model.v1.CreateUpdateBillingInstalledBaseItemResult;
import com.dreameddeath.installedbase.model.v1.common.published.query.InstalledStatusResponse;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 12/12/2017.
 */
public interface ICreateUpdateBillingInstalledBaseItemStatusService {
    CreateUpdateBillingInstalledBaseAction manageUpdateOfStatuses(BillingInstalledBaseItem billingInstalledBaseItem, List<InstalledStatusResponse> statuses, CreateUpdateBillingInstalledBaseItemResult itemUpdateResult);
}

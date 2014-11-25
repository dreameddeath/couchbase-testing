package com.dreameddeath.billing.model.process;

import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import org.joda.time.DateTime;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
public class CreateBillingCycleRequest extends BaseCouchbaseDocumentElement {
    @DocumentProperty("ba")
    public BillingAccountLink baLink;
    @DocumentProperty("startDate")
    public DateTime startDate;

}

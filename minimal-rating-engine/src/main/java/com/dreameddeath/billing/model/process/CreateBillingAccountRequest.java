package com.dreameddeath.billing.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;

/**
 * Created by ceaj8230 on 13/08/2014.
 */
public class CreateBillingAccountRequest extends BaseCouchbaseDocumentElement {
    @DocumentProperty("partyId")
    public String partyId;
    @DocumentProperty("billDay")
    public Integer billDay;
    @DocumentProperty("cycleLength")
    public Integer cycleLength;
}
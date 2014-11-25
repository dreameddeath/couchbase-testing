package com.dreameddeath.billing.process.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

/**
 * Created by ceaj8230 on 13/08/2014.
 */
public class CreateBillingAccountRequest extends CouchbaseDocumentElement {
    @DocumentProperty("partyId")
    public String partyId;
    @DocumentProperty("billDay")
    public Integer billDay;
    @DocumentProperty("cycleLength")
    public Integer cycleLength;
}

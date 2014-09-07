package com.dreameddeath.billing.model.process;

import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.party.model.base.PartyLink;

/**
 * Created by ceaj8230 on 13/08/2014.
 */
public class CreateBillingAccountResult extends CouchbaseDocumentElement {
    @DocumentProperty("baCreated")
    public BillingAccountLink baLink;
    @DocumentProperty("partyLink")
    public PartyLink partyLink;

}

package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.core.model.business.CouchbaseDocumentLink;

/**
 * Created by ceaj8230 on 13/08/2014.
 */
public class BillingInstalledBaseLink extends CouchbaseDocumentLink<BillingInstalledBase> {
    public BillingInstalledBaseLink(){super();}
    public BillingInstalledBaseLink(BillingInstalledBase target){super(target);}
    public BillingInstalledBaseLink(BillingInstalledBaseLink link){super(link);}

}

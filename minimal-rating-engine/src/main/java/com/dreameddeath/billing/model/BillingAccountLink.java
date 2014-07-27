package com.dreameddeath.billing.model;


import com.dreameddeath.common.model.document.CouchbaseDocumentLink;


public class BillingAccountLink extends CouchbaseDocumentLink<BillingAccount>{
    
    public BillingAccountLink(){}
    public BillingAccountLink (BillingAccount ba){
        super(ba);
    }
    public BillingAccountLink(BillingAccountLink srcLink){
        super(srcLink);
    }
    
    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="}\n";
        return result;
    }
}
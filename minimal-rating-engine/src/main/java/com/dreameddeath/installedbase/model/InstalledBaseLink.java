package com.dreameddeath.installedbase.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentLink;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledBaseLink extends CouchbaseDocumentLink<InstalledBase> {
    /**
     *  offerId : The offer id linked (if only a subpart is pointed by the link)
     */
    @DocumentProperty("offerId")
    private Property<String> _offerId = new StandardProperty<String>(InstalledBaseLink.this);
    /**
     *  productId : The installedProductId (if only a sub part of the installed base is pointed by the link)
     */
    @DocumentProperty("productId")
    private Property<String> _productId = new StandardProperty<String>(InstalledBaseLink.this);


    // offerId accessors
    public String getOfferId() { return _offerId.get(); }
    public void setOfferId(String val) { _offerId.set(val); }

    // productId accessors
    public String getProductId() { return _productId.get(); }
    public void setProductId(String val) { _productId.set(val); }

    public InstalledBaseLink(){}
    public InstalledBaseLink (InstalledBase installedBase){
        super(installedBase);
    }
    public InstalledBaseLink(InstalledBaseLink srcLink){
        super(srcLink);
    }

    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="}\n";
        return result;
    }
}

package com.dreameddeath.installedbase.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceaj8230 on 04/09/2014.
 */
public class CreateUpdateInstalledBaseResponse extends CouchbaseDocumentElement {
    @DocumentProperty("contracts")
    public List<Contract> contracts=new ArrayList<Contract>();
    @DocumentProperty("offers")
    public List<Offer> offers=new ArrayList<Offer>();

    public abstract static class IdentifiedItem {
        @DocumentProperty("id")
        public String id;
        @DocumentProperty("tempId")
        public String tempId;
        @DocumentProperty("installedBaseKey")
        public String installedBaseKey;
    }

    public static class Contract extends IdentifiedItem{}

    public static class Offer extends IdentifiedItem{
        @DocumentProperty("tariffs")
        public List<Tariff> tariffs=new ArrayList<Tariff>();
        @DocumentProperty("productService")
        public List<ProductService> productServices=new ArrayList<ProductService>();
    }

    public static class ProductService extends IdentifiedItem{}

    public static class Tariff extends IdentifiedItem{
        @DocumentProperty("discounts")
        public List<Discount> discounts=new ArrayList<Discount>();
    }

    public static class Discount extends IdentifiedItem{}

}

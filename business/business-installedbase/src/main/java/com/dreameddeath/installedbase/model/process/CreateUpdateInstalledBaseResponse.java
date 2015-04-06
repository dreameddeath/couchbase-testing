/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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

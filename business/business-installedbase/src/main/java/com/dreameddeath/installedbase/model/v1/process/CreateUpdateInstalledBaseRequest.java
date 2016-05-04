/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.installedbase.model.v1.process;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.validation.annotation.Unique;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemLink;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemRevision;
import com.dreameddeath.installedbase.model.v1.common.InstalledStatus;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 04/09/2014.
 */
public class CreateUpdateInstalledBaseRequest extends CouchbaseDocumentElement {
    @DocumentProperty("createRequestUid") @Unique(nameSpace = "createInstalledBaseJob")//If installed base creation, perform a duplicate check based on that
    public String creationRequestUid;
    @DocumentProperty("requestDate")
    public DateTime requestDate;
    @DocumentProperty("contracts")
    public List<Contract> contracts=new ArrayList<>();
    @DocumentProperty("offers")
    public List<Offer> offers = new ArrayList<>();

    public static abstract class Item extends CouchbaseDocumentElement{
        @DocumentProperty("status")
        public ItemStatus status=new ItemStatus();
        @DocumentProperty("orderInfo")
        public OrderItemInfo orderInfo=new OrderItemInfo();
        @DocumentProperty("commercialOperation")
        public CommercialOperation comOp;

    }

    public static abstract class IdentifiedItem extends Item{
        @DocumentProperty("id")
        public String id;
        @DocumentProperty("tempId")
        public String tempId;
        @DocumentProperty("code")
        public String code;
    }

    public static class Contract extends IdentifiedItem {
        @DocumentProperty("holderId")
        public String holderId;
        @DocumentProperty("billingAccountId")
        public String billingAccountId;
        @DocumentProperty("links")
        public List<IdentifiedItemLink> links = new ArrayList<>();
    }


    public static class IdentifiedItemLink extends CouchbaseDocumentElement{
        @DocumentProperty("commercialOperation")
        public LinkOperation comOp;
        @DocumentProperty("status")
        public ItemStatus status=new ItemStatus();
        @DocumentProperty("type")
        public LinkType linkType;
        @DocumentProperty("direction")
        public LinkDirection direction;
        @DocumentProperty("target")
        public TargetIdentifiedItem target;
    }

    public enum LinkType{
        RELIES_ON(InstalledItemLink.Type.RELIES),
        BRINGS(InstalledItemLink.Type.BRINGS),
        AGGREGATE(InstalledItemLink.Type.AGGREGATE),
        MIGRATE(InstalledItemLink.Type.MIGRATE);

        private InstalledItemLink.Type type;

        LinkType(InstalledItemLink.Type type){
            this.type=type;
        }

        public InstalledItemLink.Type getType(){
            return type;
        }

    }

    public enum LinkDirection{
        FROM(true),
        TO(null);

        private Boolean isReverse;

        LinkDirection(Boolean isReverse){
            this.isReverse=isReverse;
        }

        public Boolean isReverse() {
            return isReverse;
        }
    }

    public static class Offer extends IdentifiedItem {
        @DocumentProperty("type")
        public OfferType type;
        @DocumentProperty("tariffs")
        public List<Tariff> tariffs=new ArrayList<>();
        @DocumentProperty("attributes")
        public List<Attribute> attributes = new ArrayList<>();
        @DocumentProperty("links")
        public List<IdentifiedItemLink> links = new ArrayList<>();
        @DocumentProperty("parent")
        public TargetIdentifiedItem parent = new TargetIdentifiedItem();
        @DocumentProperty("productService")
        public ProductService ps = new ProductService();
    }

    public static class TargetIdentifiedItem extends CouchbaseDocumentElement{
        @DocumentProperty("id")
        public String id;
        @DocumentProperty("tempId")
        public String tempId;
    }

    public enum OfferType{
        CONTRACT,
        PLAY,
        SET,
        ATOMIC_OFFER
    }

    public static class ProductService extends IdentifiedItem{
        @DocumentProperty("links")
        public List<IdentifiedItemLink> links = new ArrayList<IdentifiedItemLink>();
        @DocumentProperty("attributes")
        public List<Attribute> attributes = new ArrayList<Attribute>();
    }

    public static class Tariff extends IdentifiedItem {
        @DocumentProperty("tailorMadeVal")
        public BigDecimal tailorMadeValue;
        @DocumentProperty("discounts")
        public List<Discount> discounts = new ArrayList<Discount>();
    }

    public static class Discount extends IdentifiedItem {
        @DocumentProperty("tailorMadeVal")
        public BigDecimal tailorMadeValue;
    }

    public static class Attribute extends CouchbaseDocumentElement{
        @DocumentProperty("code")
        public String code;
        @DocumentProperty("publicKeyType")
        public String type;
        @DocumentProperty("values")
        public List<Value> values=new ArrayList<>();
        @DocumentProperty("comOp")
        public AttributeOperation comOp;


        public static class Value extends CouchbaseDocumentElement{
            @DocumentProperty("value")
            public String value;
            @DocumentProperty("startDate")
            public DateTime startDate;
            @DocumentProperty("endDate")
            public DateTime endDate;
            @DocumentProperty("comOp")
            public ValueOperation comOp;

        }

    }

    public static class ItemStatus extends CouchbaseDocumentElement{
        @DocumentProperty("status")
        public Status code;
        @DocumentProperty("effectiveDate")
        public DateTime effectiveDate;
    }

    public enum Status{
        ACTIVE(InstalledStatus.Code.ACTIVE),
        SUSPENDED(InstalledStatus.Code.SUSPENDED),
        REMOVED(InstalledStatus.Code.REMOVED),
        CLOSED(InstalledStatus.Code.CLOSED),
        ABORTED(InstalledStatus.Code.ABORTED);

        private final InstalledStatus.Code mappedCode;

        Status(InstalledStatus.Code code){
            mappedCode = code;
        }

        public boolean isSameStatus(InstalledStatus.Code code){
            return this.mappedCode.equals(code);
        }

        public InstalledStatus.Code getMappedCode(){
            return mappedCode;
        }
    }

    public static class OrderItemInfo extends CouchbaseDocumentElement{
        @DocumentProperty("orderItemId")
        public String orderItemId;
        @DocumentProperty("orderId")
        public String orderId;
        @DocumentProperty("status")
        public OrderStatus status;
        @DocumentProperty("effectiveDate")
        public DateTime effectiveDate;
    }

    public enum OrderStatus{
        IN_ORDER,
        ON_DELIVERY,
        COMPLETED,
        CANCELLED;

        public boolean isRevTarget(InstalledItemRevision.RevState revState){
            switch(this){
                case IN_ORDER:
                case ON_DELIVERY:
                    return revState.equals(InstalledItemRevision.RevState.REQUESTED);
                case CANCELLED:
                    return revState.equals(InstalledItemRevision.RevState.CANCELLED);
                case COMPLETED:
                    return revState.equals(InstalledItemRevision.RevState.PLANNED)|| revState.equals(InstalledItemRevision.RevState.DONE);
                default:
                    return false;
            }
        }

        public boolean isUpdatableFrom(InstalledItemRevision.RevState revState){
            if(revState ==null){
                return true;
            }
            if(isRevTarget(revState)){
                return true;
            }
            switch(this){
                case IN_ORDER: case ON_DELIVERY:
                    return revState.equals(InstalledItemRevision.RevState.PLANNED);
                case CANCELLED:
                    return revState.equals(InstalledItemRevision.RevState.REQUESTED) ||
                            revState.equals(InstalledItemRevision.RevState.PLANNED);
                case COMPLETED:
                    return revState.equals(InstalledItemRevision.RevState.PLANNED);
                default:
                    return false;
            }
        }

        public InstalledItemRevision.RevState toRevState(){
            for(InstalledItemRevision.RevState target:InstalledItemRevision.RevState.values()){
                if(isRevTarget(target)){
                    return target;
                }
            }
            return null;
        }
    }

    public enum CommercialOperation{
        ADD,
        REMOVE,
        MOVE,
        CHANGE,
        CHILD_CHANGE,
        MIGRATE,
        ACTIVATE,
        SUSPEND,
        UNCHANGE,
        CANCEL
    }

    public enum ValueOperation{
        ADD,
        MODIFY,
        REMOVE,
        UNCHANGE
    }

    public enum AttributeOperation{
        ADD,
        MODIFY,
        REMOVE,
        UNCHANGE
    }

    public enum LinkOperation{
        ADD,
        MODIFY,
        REMOVE,
        UNCHANGE
    }
}

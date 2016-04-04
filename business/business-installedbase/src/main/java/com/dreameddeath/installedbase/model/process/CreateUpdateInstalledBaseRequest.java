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

package com.dreameddeath.installedbase.model.process;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.validation.annotation.Unique;
import com.dreameddeath.installedbase.model.common.InstalledItemLink;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;
import com.dreameddeath.installedbase.model.common.InstalledStatus;
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

    public abstract class Item {
        @DocumentProperty("status")
        public ItemStatus status=new ItemStatus();
        @DocumentProperty("orderInfo")
        public OrderItemInfo orderInfo=new OrderItemInfo();
        @DocumentProperty("commercialOperation")
        public CommercialOperation comOp;

    }

    public abstract class IdentifiedItem extends Item{
        @DocumentProperty("id")
        public String id;
        @DocumentProperty("tempId")
        public String tempId;
        @DocumentProperty("code")
        public String code;
    }

    public class Contract extends IdentifiedItem {
        @DocumentProperty("holderId")
        public String holderId;
        @DocumentProperty("billingAccountId")
        public String billingAccountId;
        @DocumentProperty("links")
        public List<IdentifiedItemLink> links = new ArrayList<>();
    }


    public class IdentifiedItemLink{
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
        RELIES_ON(InstalledItemLink.Type.RELIES,InstalledItemLink.Direction.TO),
        BRINGS(InstalledItemLink.Type.BRINGS,InstalledItemLink.Direction.TO),
        AGGREGATE(InstalledItemLink.Type.AGGREGATE,InstalledItemLink.Direction.TO),
        MIGRATE(InstalledItemLink.Type.MIGRATE,InstalledItemLink.Direction.TO),;

        private InstalledItemLink.Type type;
        private InstalledItemLink.Direction direction;

        LinkType(InstalledItemLink.Type type,InstalledItemLink.Direction direction){
            this.type=type;
            this.direction = direction;
        }

        public InstalledItemLink.Type getType(){
            return type;
        }

        public InstalledItemLink.Direction getDirection(){
            return direction;
        }
    }

    public enum LinkDirection{
        FROM(InstalledItemLink.Direction.FROM),
        TO(InstalledItemLink.Direction.TO);

        private InstalledItemLink.Direction direction;

        LinkDirection(InstalledItemLink.Direction direction){
            this.direction=direction;
        }

        public InstalledItemLink.Direction getDirection() {
            return direction;
        }
    }

    public class Offer extends IdentifiedItem {
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

    public class TargetIdentifiedItem {
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

    public class ProductService extends IdentifiedItem{
        @DocumentProperty("links")
        public List<IdentifiedItemLink> links = new ArrayList<IdentifiedItemLink>();
        @DocumentProperty("attributes")
        public List<Attribute> attributes = new ArrayList<Attribute>();
    }

    public class Tariff extends IdentifiedItem {
        @DocumentProperty("tailorMadeVal")
        public BigDecimal tailorMadeValue;
        @DocumentProperty("discounts")
        public List<Discount> discounts = new ArrayList<Discount>();
    }

    public class Discount extends IdentifiedItem {
        @DocumentProperty("tailorMadeVal")
        public BigDecimal tailorMadeValue;
    }

    public class Attribute{
        @DocumentProperty("code")
        public String code;
        @DocumentProperty("publicKeyType")
        public String type;
        @DocumentProperty("values")
        public List<Value> values=new ArrayList<>();
        @DocumentProperty("comOp")
        public AttributeOperation comOp;


        public class Value{
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

    public class ItemStatus {
        @DocumentProperty("status")
        public Status code;
        @DocumentProperty("effectiveDate")
        public DateTime effectiveDate;
        @DocumentProperty("endDate")
        public DateTime endDate;
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

    public class OrderItemInfo{
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

        public boolean isRevTarget(InstalledItemRevision.RevStatus revStatus){
            switch(this){
                case IN_ORDER:
                case ON_DELIVERY:
                    return revStatus.equals(InstalledItemRevision.RevStatus.REQUESTED);
                case CANCELLED:
                    return revStatus.equals(InstalledItemRevision.RevStatus.CANCELLED);
                case COMPLETED:
                    return revStatus.equals(InstalledItemRevision.RevStatus.PLANNED)|| revStatus.equals(InstalledItemRevision.RevStatus.CURRENT);
                default:
                    return false;
            }
        }

        public boolean isUpdatableFrom(InstalledItemRevision.RevStatus revStatus){
            if(revStatus==null){
                return true;
            }
            if(isRevTarget(revStatus)){
                return true;
            }
            switch(this){
                case IN_ORDER: case ON_DELIVERY:
                    return revStatus.equals(InstalledItemRevision.RevStatus.PLANNED);
                case CANCELLED:
                    return revStatus.equals(InstalledItemRevision.RevStatus.REQUESTED) ||
                            revStatus.equals(InstalledItemRevision.RevStatus.PLANNED);
                case COMPLETED:
                    return revStatus.equals(InstalledItemRevision.RevStatus.PLANNED);
                default:
                    return false;
            }
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

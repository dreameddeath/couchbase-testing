package com.dreameddeath.installedbase.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.Unique;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceaj8230 on 04/09/2014.
 */
public class CreateUpdateInstalledBaseRequest extends BaseCouchbaseDocumentElement {
    @DocumentProperty("createRequestUid") @Unique(nameSpace = "createInstalledBaseJob")//If installed base creation, perform a duplicate check based on that
    public String creationRequestUid;
    @DocumentProperty("requestDate")
    public DateTime requestDate;
    @DocumentProperty("contracts")
    public List<Contract> contracts=new ArrayList<Contract>();
    @DocumentProperty("offers")
    public List<Offer> offers = new ArrayList<Offer>();

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
    }

    public class Contract extends IdentifiedItem {
        @DocumentProperty("holderId")
        public String holderId;
        @DocumentProperty("billingAccountId")
        public String billingAccountId;
        @DocumentProperty("links")
        public List<IdentifiedItemLink> links = new ArrayList<IdentifiedItemLink>();
    }


    public class IdentifiedItemLink extends Item{
        @DocumentProperty("type")
        public LinkType linkType;
        @DocumentProperty("direction")
        public LinkDirection direction;
        @DocumentProperty("id")
        public String id;
        @DocumentProperty("tempId")
        public String tempId;
    }

    public enum LinkType{
        RELIES_ON,
        BRINGS,
        AGGREGATE,
        MIGRATE
    }

    public enum LinkDirection{
        FROM,
        TO
    }

    public class Offer extends IdentifiedItem {
        @DocumentProperty("spec")
        public Specification spec=new Specification();
        @DocumentProperty("tariffs")
        public List<Tariff> tariffs=new ArrayList<Tariff>();
        @DocumentProperty("attributes")
        public List<Attribute> attributes = new ArrayList<Attribute>();
        @DocumentProperty("links")
        public List<IdentifiedItemLink> links = new ArrayList<IdentifiedItemLink>();
        @DocumentProperty("parent")
        public ParentIdentifiedItem parent = new ParentIdentifiedItem();
        @DocumentProperty("productService")
        public ProductService ps = new ProductService();

        public class Specification{
            @DocumentProperty("code")
            public String code;
            @DocumentProperty("type")
            public OfferType type;
        }
    }

    public class ParentIdentifiedItem{
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
        @DocumentProperty("spec")
        public Specification spec = new Specification();

        public class Specification{
            @DocumentProperty("code")
            public String code;
        }
    }

    public class Tariff extends IdentifiedItem {
        @DocumentProperty("spec")
        public Specification spec=new Specification();
        @DocumentProperty("discounts")
        public List<Discount> discounts = new ArrayList<Discount>();

        public class Specification{
            @DocumentProperty("code")
            public String code;
            @DocumentProperty("tailorMadeVal")
            public BigDecimal tailorMadeValue;
        }
    }

    public class Discount extends IdentifiedItem {
        @DocumentProperty("spec")
        public Specification spec=new Specification();

        public class Specification{
            @DocumentProperty("code")
            public String code;
            @DocumentProperty("tailorMadeVal")
            public BigDecimal tailorMadeValue;
        }
    }

    public class Attribute extends Item{
        @DocumentProperty("spec")
        public Specification spec=new Specification();
        @DocumentProperty("values")
        public List<Value> values=new ArrayList<Value>();

        public class Value extends Item{
            @DocumentProperty("value")
            public String value;
            @DocumentProperty("spec")
            public Specification spec=new Specification();

            public class Specification{
                @DocumentProperty("code")
                public String code;
            }
        }

        public class Specification{
            @DocumentProperty("code")
            public String code;
            @DocumentProperty("publicKey")
            public PublicKeySpec keySpec=new PublicKeySpec();

            public class PublicKeySpec{
                @DocumentProperty("type")
                public String type;
            }
        }
    }

    public class ItemStatus {
        @DocumentProperty("status")
        public Status statusCode;
        @DocumentProperty("startDate")
        public DateTime startDate;
        @DocumentProperty("endDate")
        public DateTime endDate;
    }

    public enum Status{
        ACTIVE,
        SUSPENDED,
        REMOVED,
        CLOSED,
        CANCELLED
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
        CANCELLED
    }

    public enum CommercialOperation{
        ADD,
        REMOVE,
        CHANGE,
        MIGRATE,
        ACTIVATE,
        SUSPEND,
        UNCHANGE,
        CANCEL
    }

}

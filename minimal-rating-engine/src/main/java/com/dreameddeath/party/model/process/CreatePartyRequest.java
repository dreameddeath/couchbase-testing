package com.dreameddeath.party.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class CreatePartyRequest extends BaseCouchbaseDocumentElement {
    @DocumentProperty("type")
    public Type type;
    @DocumentProperty("person")
    public Person person;
    @DocumentProperty("organization")
    public Organization organization;

    public static class Person extends BaseCouchbaseDocumentElement {
        @DocumentProperty("firstName")
        public String firstName;
        @DocumentProperty("lastName")
        public String lastName;
    }

    public static class Organization extends BaseCouchbaseDocumentElement {
        @DocumentProperty("brand")
        public String brand;
        @DocumentProperty("tradingName")
        public String tradingName;
    }

    public enum Type{
        person, organization
    }
}

package com.dreameddeath.party.process.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.RawCouchbaseDocumentElement;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class CreatePartyRequest extends RawCouchbaseDocumentElement {
    @DocumentProperty("type")
    public Type type;
    @DocumentProperty("person")
    public Person person;
    @DocumentProperty("organization")
    public Organization organization;

    public static class Person extends RawCouchbaseDocumentElement {
        @DocumentProperty("firstName")
        public String firstName;
        @DocumentProperty("lastName")
        public String lastName;
    }

    public static class Organization extends RawCouchbaseDocumentElement {
        @DocumentProperty("brand")
        public String brand;
        @DocumentProperty("tradingName")
        public String tradingName;
    }

    public enum Type{
        person, organization
    }
}

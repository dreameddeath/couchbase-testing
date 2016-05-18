package com.dreameddeath.party.process.model.v1.roles.tasks;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
public class PartyUpdateResult extends CouchbaseDocumentElement{
    /**
     *  uid : party uid
     */
    @DocumentProperty("uid")
    private Property<String> uid = new StandardProperty<>(PartyUpdateResult.this);

    /**
     * Getter of uid
     * @return the value of uid
     */
    public String getUid() { return uid.get(); }
    /**
     * Setter of uid
     * @param val the new value of uid
     */
    public void setUid(String val) { uid.set(val); }
}

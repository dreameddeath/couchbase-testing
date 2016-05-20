package com.dreameddeath.billing.model.v1.account;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

/**
 * Created by Christophe Jeunesse on 19/05/2016.
 */
public class PartyRoleLink extends CouchbaseDocumentElement{
    /**
     *  pid : the party id
     */
    @DocumentProperty("pid")
    private Property<String> pid = new ImmutableProperty<>(PartyRoleLink.this);
    /**
     *  roleUid : the linked role uid
     */
    @DocumentProperty("roleUid")
    private Property<String> roleUid = new ImmutableProperty<>(PartyRoleLink.this);

    /**
     * Getter of pid
     * @return the value of pid
     */
    public String getPid() { return pid.get(); }
    /**
     * Setter of pid
     * @param val the new value for pid
     */
    public void setPid(String val) { pid.set(val); }

    /**
     * Getter of roleUid
     * @return the value of roleUid
     */
    public String getRoleUid() { return roleUid.get(); }
    /**
     * Setter of roleUid
     * @param val the new value for roleUid
     */
    public void setRoleUid(String val) { roleUid.set(val); }
}

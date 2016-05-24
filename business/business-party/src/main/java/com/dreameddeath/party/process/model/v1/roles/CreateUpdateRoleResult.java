package com.dreameddeath.party.process.model.v1.roles;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

/**
 * Created by Christophe Jeunesse on 19/05/2016.
 */
public class CreateUpdateRoleResult extends CouchbaseDocumentElement {
    /**
     *  tempUid : The temporary id of the created role if any
     */
    @DocumentProperty("tempUid")
    private Property<String> tempUid = new ImmutableProperty<>(CreateUpdateRoleResult.this);
    /**
     *  roleUid : the updated role uid
     */
    @DocumentProperty("roleUid")
    private Property<String> roleUid = new ImmutableProperty<>(CreateUpdateRoleResult.this);

    /**
     * Getter of tempUid
     * @return the value of tempUid
     */
    public String getTempUid() { return tempUid.get(); }
    /**
     * Setter of tempUid
     * @param val the new value for tempUid
     */
    public void setTempUid(String val) { tempUid.set(val); }

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

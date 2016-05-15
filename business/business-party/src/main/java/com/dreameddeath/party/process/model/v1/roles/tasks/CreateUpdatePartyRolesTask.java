package com.dreameddeath.party.process.model.v1.roles.tasks;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.process.model.v1.tasks.DocumentUpdateTask;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.party.model.v1.Party;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
@DocumentEntity
public class CreateUpdatePartyRolesTask extends DocumentUpdateTask<Party> {
    /**
     *  partyId : party id to update the roles for
     */
    @DocumentProperty("partyId") @NotNull
    private Property<String> partyId = new ImmutableProperty<>(CreateUpdatePartyRolesTask.this);

    /**
     * Getter of partyId
     * @return the value of partyId
     */
    public String getPartyId() { return partyId.get(); }
    /**
     * Setter of partyId
     * @param val the new value of partyId
     */
    public void setPartyId(String val) { partyId.set(val); }

}

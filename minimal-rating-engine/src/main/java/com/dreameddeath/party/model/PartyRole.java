package com.dreameddeath.party.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ImmutableProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public abstract class PartyRole extends CouchbaseDocumentElement {
    @DocumentProperty("uid")
    private ImmutableProperty<String> _uid=new ImmutableProperty<String>(PartyRole.this);
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate = new StandardProperty<DateTime>(PartyRole.this);
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate= new StandardProperty<DateTime>(PartyRole.this);


    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }

    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime startDate) { _startDate.set(startDate); }

    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime endDate) { _endDate.set(endDate); }


}

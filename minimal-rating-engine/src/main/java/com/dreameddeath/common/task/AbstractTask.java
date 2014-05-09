package com.dreameddeath.common.task;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.CouchbaseDocumentElement;
import com.dreameddeath.common.model.ImmutableProperty;
import com.dreameddeath.common.model.Property;
import com.dreameddeath.common.model.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.joda.time.DateTime;
import java.util.UUID;

@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractTask extends CouchbaseDocumentElement{
    @DocumentProperty("uid")
    private Property<UUID> _uid=new ImmutableProperty<UUID>(AbstractTask.this,UUID.randomUUID());
    @DocumentProperty("state")
    private Property<State> _state=new StandardProperty<State>(AbstractTask.this,State.PENDING);
    @DocumentProperty("effectiveDate")
    private Property<DateTime> _effectiveDate=new StandardProperty<DateTime>(AbstractTask.this);
    
    public State getState() { return _state.get(); }
    public void setState(State state) { _state.set(state); }
    
    public UUID getUid() { return _uid.get(); }
    public void setUid(UUID uid) { _uid.set(uid); }
    
    public DateTime getEffectiveDate() { return _effectiveDate.get(); }
    public void setEffectiveDate(DateTime date) { _effectiveDate.set(date); }
    
    
    
    public static enum State{
        PENDING,
        DONE;
    }
}
package com.dreameddeath.common.task;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.joda.time.DateTime;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)
@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractTask{
    @DocumentProperty("uid")
    private UUID _uid=UUID.randomUUID();
    @DocumentProperty("state")
    private State _state=State.PENDING;
    @DocumentProperty("effectiveDate")
    private DateTime _effectiveDate;
    
    public State getState() { return _state; }
    public void setState(State state) { _state=state; }
    
    public UUID getUid() { return _uid; }
    public void setUid(UUID uid) { _uid=uid; }
    
    public DateTime getEffectiveDate() { return _effectiveDate; }
    public void setEffectiveDate(DateTime date) { _effectiveDate=date; }
    
    
    
    public static enum State{
        PENDING,
        DONE;
    }
}
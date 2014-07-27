package com.dreameddeath.common.model.process;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocumentElement;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;
import com.dreameddeath.common.model.property.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.joda.time.DateTime;

@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class AbstractTask extends CouchbaseDocumentElement{
    private AbstractJob _parentJob;
    @DocumentProperty("uid")
    private Property<String> _uid=new ImmutableProperty<String>(AbstractTask.this);
    @DocumentProperty("label")
    private Property<String> _label=new StandardProperty<String>(AbstractTask.this);
    @DocumentProperty("state")
    private Property<State> _state=new StandardProperty<State>(AbstractTask.this,State.INITIALIZED);
    @DocumentProperty("effectiveDate")
    private Property<DateTime> _effectiveDate=new StandardProperty<DateTime>(AbstractTask.this);


    public void setParentJob(AbstractJob job){ _parentJob = job;}
    public AbstractJob getParentJob(){ return _parentJob;}
    public <T extends AbstractJob> T getParentJob(Class<T> clazz){ return (T)_parentJob;}


    public State getState() { return _state.get(); }
    public void setState(State state) { _state.set(state); }
    public boolean isInitialized(){ return _state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return _state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return _state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return _state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isDone(){ return _state.get().compareTo(State.DONE)>=0; }

    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }

    public String getLabel(){ return _label.get(); }
    public void setLabel(String label){ _label.set(label); }
    
    public static enum State{
        INITIALIZED, //Init done
        PREPROCESSED,
        PROCESSED, //Processing Done
        POSTPROCESSED, //Job Update Processing done
        DONE;//Cleaning done
    }


    public void init(){}
    public void preprocess(){}
    public void process(){}
    public void postprocess(){}
    public void finalize(){}
    public void cleanup(){}
}
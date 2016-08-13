package com.dreameddeath.core.notification.model.v1.listener;

import com.dreameddeath.core.curator.model.IRegisterable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 25/05/2016.
 */
public class ListenerDescription implements IRegisterable {
    @JsonProperty("name")
    private String name;
    @JsonProperty("listenedEvents")
    private List<ListenedEvent> listenedEvents =new ArrayList<>();
    @JsonProperty("type")
    private String type;
    @JsonProperty("allowDeferred")
    private Boolean allowDeferred;
    @JsonProperty("params")
    private Map<String,String> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<ListenedEvent> getListenedEvents() {
        return Collections.unmodifiableList(listenedEvents);
    }

    public void setListenedEvents(Collection<ListenedEvent> listenedEvents) {
        this.listenedEvents.clear();
        this.listenedEvents.addAll(listenedEvents);
    }

    public void addListenedEvent(ListenedEvent listenedEvent) {
        this.listenedEvents.add(listenedEvent);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getAllowDeferred() {
        return allowDeferred;
    }

    public void setAllowDeferred(Boolean allowDeferred) {
        this.allowDeferred = allowDeferred;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @JsonIgnore
    public String getUid(){
        return name;
    }
}

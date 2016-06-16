package com.dreameddeath.core.notification.model.v1.listener;

import com.dreameddeath.core.curator.model.IRegisterable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 25/05/2016.
 */
public class ListenerDescription implements IRegisterable {
    @JsonProperty("groupName")
    private String groupName;
    @JsonProperty("listenedEvents")
    private List<ListenedEvent> listenedNotification=new ArrayList<>();
    @JsonProperty("type")
    private String type;


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Collection<ListenedEvent> getListenedNotification() {
        return Collections.unmodifiableList(listenedNotification);
    }

    public void setListenedNotification(Collection<ListenedEvent> listenedNotification) {
        this.listenedNotification.clear();
        this.listenedNotification.addAll(listenedNotification);
    }

    public void addListenedNotification(ListenedEvent listenedNotification) {
        this.listenedNotification.add(listenedNotification);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @JsonIgnore
    public String getUid(){
        return groupName;
    }
}

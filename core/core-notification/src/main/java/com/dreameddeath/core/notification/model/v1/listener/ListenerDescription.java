/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.notification.model.v1.listener;

import com.dreameddeath.core.curator.model.IRegistrablePathData;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.model.v1.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 25/05/2016.
 */
public class ListenerDescription implements IRegistrablePathData {
    private static final String DEFAULT_VERSION = "1.0.0";

    @JsonProperty("domain")
    private String domain;
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
    @JsonProperty("version")
    private String version;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

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
    @Override
    public String uid(){
        return getDomain()+"_"+getName()+"_"+getVersion();
    }

    @JsonIgnore
    @Override
    public String version(){
        return StringUtils.isEmpty(version)?DEFAULT_VERSION:version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version();
    }

    public void addParameter(String key, String value) {
        if(parameters==null){
            parameters = new TreeMap<>();
        }
        parameters.put(key,value);
    }


    public <T extends IEvent> boolean isApplicableEvent(Class<T> eventClazz){
        ListenedEvent listenedEvent;
        if(Event.class.isAssignableFrom(eventClazz)){
            listenedEvent = ListenedEvent.buildFromInternal((Class<? extends Event>)eventClazz);
        }
        else{
            listenedEvent = ListenedEvent.buildFromPublic(eventClazz);
        }

        for(ListenedEvent currListenedEvent:listenedEvents){
            if(currListenedEvent.getType().equals(listenedEvent.getType())){
                return true;
            }
        }
        return false;
    }


}

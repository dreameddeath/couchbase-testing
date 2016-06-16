package com.dreameddeath.core.notification.model.v1.listener;

/**
 * Created by Christophe Jeunesse on 25/05/2016.
 */
public enum ListenerRequirement {
    BEST_EFFORT(false,true),
    ASYNC(false,false),
    REALTIME(true,false);

    private boolean isBlocking;
    private boolean isOptional;

    ListenerRequirement(boolean isBlocking,boolean isOptional){
        this.isBlocking=isBlocking;
        this.isOptional=isOptional;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public boolean isOptional() {
        return isOptional;
    }
}

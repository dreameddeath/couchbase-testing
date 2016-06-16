package com.dreameddeath.core.notification.listener;

import com.dreameddeath.core.notification.model.v1.Notification;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class SubmissionResult {
    private Notification notification;
    private Throwable error;

    public SubmissionResult(Notification notif){
        this(notif,null);
    }

    public SubmissionResult(Notification notification,Throwable e){
        this.notification = notification;
        this.error = e;
    }

    public String getListenerName() {
        return notification.getListenerName();
    }

    public boolean isSuccess() {
        return error==null;
    }

    public boolean isFailure(){
        return error!=null;
    }

    public Throwable getError(){
        return error;
    }
}

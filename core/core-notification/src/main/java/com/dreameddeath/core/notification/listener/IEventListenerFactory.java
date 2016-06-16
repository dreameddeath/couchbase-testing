package com.dreameddeath.core.notification.listener;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public interface IEventListenerFactory {
    IEventListener getListener(String type,String name);
}

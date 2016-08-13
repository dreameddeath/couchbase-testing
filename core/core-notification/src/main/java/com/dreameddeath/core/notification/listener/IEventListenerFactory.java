package com.dreameddeath.core.notification.listener;

import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public interface IEventListenerFactory {
    IEventListener getListener(String type,String name,Map<String,String> params);
    IEventListener getListener(ListenerDescription description);
}

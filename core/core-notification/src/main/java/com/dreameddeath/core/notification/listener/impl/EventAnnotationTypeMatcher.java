/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.IEventListenerTypeMatcher;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 16/08/2016.
 */
public class EventAnnotationTypeMatcher implements IEventListenerTypeMatcher {
    private final String[] listType;
    private final int rank;

    public EventAnnotationTypeMatcher(Class<? extends IEventListener> listenerClass){
        Listener annot = listenerClass.getAnnotation(Listener.class);
        Preconditions.checkNotNull(annot,"Cannot find annotation listener in class {}",listenerClass.getName());
        listType = annot.forTypes();
        rank = annot.matcherRank();
    }

    private boolean isTypeMatching(String type){
        for(String allowedType:listType){
            if(allowedType.equals(type)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMatching(String type, Map<String, String> params) {
        return isTypeMatching(type);
    }

    @Override
    public boolean isMatching(ListenerDescription description) {
        return isTypeMatching(description.getType());
    }

    @Override
    public int getMatchingRank(String type, Map<String, String> params) {
        return rank;
    }

    @Override
    public int getMatchingRank(ListenerDescription description) {
        return rank;
    }
}

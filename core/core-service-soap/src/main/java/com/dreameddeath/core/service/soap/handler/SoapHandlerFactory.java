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

package com.dreameddeath.core.service.soap.handler;

import javax.annotation.Priority;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 16/09/2016.
 */
public class SoapHandlerFactory {
    private final List<Handler> handlerList=new ArrayList<>();

    public void addHandler(Handler handler){
        handlerList.add(handler);
    }

    private int compareHandlerServerPriority(Handler a, Handler b){
        int priorityA=-1;
        int priorityB=-1;
        if(a.getClass().getAnnotation(Priority.class)!=null){
            priorityA = a.getClass().getAnnotation(Priority.class).value();
        }

        if(b.getClass().getAnnotation(Priority.class)!=null){
            priorityB = b.getClass().getAnnotation(Priority.class).value();
        }

        return (priorityA<0 && priorityB<0)?0:(priorityB-priorityA);
    }

    private int compareHandlerClientPriority(Handler a, Handler b){
        return compareHandlerServerPriority(a,b)*(-1);
    }

    public List<Handler> getHandlerList(boolean isClient){
        if(isClient) {
            return handlerList.stream().sorted(this::compareHandlerClientPriority).collect(Collectors.toList());
        }
        else{
            return handlerList.stream().sorted(this::compareHandlerServerPriority).collect(Collectors.toList());
        }
    }
}

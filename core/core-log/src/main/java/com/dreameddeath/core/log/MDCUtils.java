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

package com.dreameddeath.core.log;

import org.slf4j.MDC;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 11/09/2016.
 */
public class MDCUtils {
    public static final String USER_ID = "USER-ID";
    public static final String TRACE_ID = "TRACE-ID";
    public static final String GLOBAL_TRACE_ID = "GLOBAL-TRACE-ID";

    public static void setUserId(String userId){
        MDC.put(USER_ID,userId);
    }

    public static String getUserId(){
        return MDC.get(USER_ID);
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID,traceId);
    }

    public static String getGlobalTraceId(){return MDC.get(GLOBAL_TRACE_ID);}

    public static void setGlobalTraceId(String traceId){MDC.put(GLOBAL_TRACE_ID,traceId);}

    public static Map<String,String> getMdcContext(){
        return MDC.getCopyOfContextMap();
    }

    public static void setContextMap(Map<String, String> propertyMap) {
        if(propertyMap==null || propertyMap.isEmpty()){
            MDC.clear();
        }
        else {
            MDC.setContextMap(propertyMap);
        }
    }
}
